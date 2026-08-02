package com.itau.pix_received_publisher.infrastructure.schedulers;

import com.itau.pix_received_publisher.core.application.ports.output.messaging.MessageBrokerPort;
import com.itau.pix_received_publisher.core.application.ports.output.repositories.OutboxRepository;
import com.itau.pix_received_publisher.core.domain.entities.OutboxEvent;
import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRetryScheduler.class);

    private final OutboxRepository outboxRepository;
    private final MessageBrokerPort messageBrokerPort;
    private final MeterRegistry meterRegistry;

    @Value("${publisher.outbox.retry-interval-ms:200}")
    private long retryIntervalMs;

    @Value("${publisher.outbox.max-tentativas:5}")
    private int maxTentativas;

    @Value("${publisher.outbox.batch-size:500}")
    private int batchSize;

    private final Counter publishFailuresCounter;
    private final Counter failedPermanentCounter;
    private final Counter outboxWriteFailuresCounter;

    public OutboxRetryScheduler(OutboxRepository outboxRepository,
                               MessageBrokerPort messageBrokerPort,
                               MeterRegistry meterRegistry) {
        this.outboxRepository = outboxRepository;
        this.messageBrokerPort = messageBrokerPort;
        this.meterRegistry = meterRegistry;

        this.publishFailuresCounter = Counter.builder("pix.publisher.outbox.publish.failures")
                .description("Tentativas de publicação que falharam")
                .register(meterRegistry);

        this.failedPermanentCounter = Counter.builder("pix.publisher.outbox.failed.permanent")
                .description("Eventos que esgotaram tentativas")
                .register(meterRegistry);

        this.outboxWriteFailuresCounter = Counter.builder("pix.publisher.outbox.write.failures")
                .description("Falha ao gravar no outbox")
                .register(meterRegistry);

        Gauge.builder("pix.publisher.outbox.pending", outboxRepository, repo -> repo.countByStatus(OutboxStatus.PENDENTE))
                .description("Tamanho atual da fila PENDENTE")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${publisher.outbox.retry-interval-ms:200}")
    @Transactional
    public void publicarPendentes() {
        long batchStart = System.currentTimeMillis();
        try {
            List<OutboxEvent> pendentes = outboxRepository.findByStatus(OutboxStatus.PENDENTE, batchSize);

            if (pendentes.isEmpty()) {
                return;
            }

            log.debug("Processando {} eventos pendentes", pendentes.size());

            for (OutboxEvent evento : pendentes) {
                try {
                    long eventStart = System.currentTimeMillis();
                    messageBrokerPort.publish(evento.getTopico(), evento.getChaveParticao(), evento.getPayload());
                    evento.setStatus(OutboxStatus.PUBLICADO);
                    evento.setPublicadoEm(java.time.Instant.now());
                    outboxRepository.save(evento);
                    
                    long eventDuration = System.currentTimeMillis() - eventStart;
                    long totalLatency = java.time.Duration.between(evento.getCriadoEm(), evento.getPublicadoEm()).toMillis();
                    
                    log.debug("Evento {} publicado com sucesso no tópico {} (publicação: {}ms, latência total: {}ms)", 
                        evento.getId(), evento.getTopico(), eventDuration, totalLatency);
                } catch (Exception e) {
                    evento.setTentativas(evento.getTentativas() + 1);
                    if (evento.getTentativas() >= maxTentativas) {
                        evento.setStatus(OutboxStatus.FALHA_DEFINITIVA);
                        failedPermanentCounter.increment();
                        log.error("Evento {} esgotou tentativas de publicação", evento.getId());
                    } else {
                        publishFailuresCounter.increment();
                        log.warn("Falha ao publicar evento {}, tentativa {}/{}", evento.getId(), evento.getTentativas(), maxTentativas);
                    }
                    outboxRepository.save(evento);
                }
            }
            
            long batchDuration = System.currentTimeMillis() - batchStart;
            if (batchDuration > 10) {
                log.debug("Batch de {} eventos publicado em {}ms (avg: {}ms/evento)", 
                    pendentes.size(), batchDuration, batchDuration / pendentes.size());
            }
        } catch (Exception e) {
            outboxWriteFailuresCounter.increment();
            log.error("Erro ao processar fila de outbox", e);
        }
    }
}
