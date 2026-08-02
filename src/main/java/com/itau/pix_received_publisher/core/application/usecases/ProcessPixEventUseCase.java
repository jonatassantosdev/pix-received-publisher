package com.itau.pix_received_publisher.core.application.usecases;

import com.itau.pix_received_publisher.core.application.dtos.PixWebhookBatchRequest;
import com.itau.pix_received_publisher.core.application.exceptions.ValidationException;
import com.itau.pix_received_publisher.core.application.mappers.PixEventMapper;
import com.itau.pix_received_publisher.core.application.ports.output.services.EventSerializerPort;
import com.itau.pix_received_publisher.core.application.ports.output.services.EventValidatorPort;
import com.itau.pix_received_publisher.core.application.ports.output.repositories.OutboxRepository;
import com.itau.pix_received_publisher.core.domain.entities.OutboxEvent;
import com.itau.pix_received_publisher.core.domain.models.PixReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessPixEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPixEventUseCase.class);

    private final OutboxRepository outboxRepository;
    private final EventSerializerPort serializer;
    private final EventValidatorPort validator;

    public ProcessPixEventUseCase(OutboxRepository outboxRepository,
                                  EventSerializerPort serializer,
                                  EventValidatorPort validator) {
        this.outboxRepository = outboxRepository;
        this.serializer = serializer;
        this.validator = validator;
    }

    @Transactional
    public int executeBatch(PixWebhookBatchRequest batch) {
        int processedCount = 0;
        long batchStart = System.currentTimeMillis();
        
        for (var request : batch.pix()) {
            PixReceivedEvent evento = PixEventMapper.toDomain(request);
            
            if (!validator.isValid(evento)) {
                throw new ValidationException("Evento Pix rejeitado na validação de contrato: " + evento);
            }

            String payload = serializer.serialize(evento);
            OutboxEvent outboxEvent = new OutboxEvent("pix.payment.request.received", evento.endToEndId(), payload);
            
            outboxRepository.save(outboxEvent);
            
            processedCount++;
        }
        
        long batchDuration = System.currentTimeMillis() - batchStart;
        if (batchDuration > 10) {
            log.debug("Batch de {} eventos processado em {}ms (avg: {}ms/evento)", 
                processedCount, batchDuration, batchDuration / processedCount);
        }
        
        return processedCount;
    }
}
