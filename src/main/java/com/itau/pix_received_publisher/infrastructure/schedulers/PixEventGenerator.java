package com.itau.pix_received_publisher.infrastructure.schedulers;

import com.itau.pix_received_publisher.core.application.dtos.PixReceivedEventRequest;
import com.itau.pix_received_publisher.core.application.dtos.PixWebhookBatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PixEventGenerator {

    private static final Logger log = LoggerFactory.getLogger(PixEventGenerator.class);

    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final AtomicLong startTime = new AtomicLong(0);

    @Value("${publisher.simulador.enabled:true}")
    private boolean simuladorEnabled;

    @Value("${publisher.simulador.tick-ms:100}")
    private int tickMs;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${publisher.simulador.cobranca-service-url:http://localhost:8082}")
    private String cobrancaServiceUrl;

    @Value("${publisher.simulador.criar-cobranca:false}")
    private boolean criarCobranca;

    // Cenário de teste: 2k/s (60s) -> 7k/s (15s) -> 2k/s (60s)
    private static final int BASE_RATE = 2000; // eventos por segundo
    private static final int PEAK_RATE = 7000; // eventos por segundo
    private static final long PHASE1_DURATION_MS = 60_000; // 1 minuto
    private static final long PHASE2_DURATION_MS = 15_000; // 15 segundos
    private static final long PHASE3_DURATION_MS = 60_000; // 1 minuto
    private static final long TOTAL_DURATION_MS = PHASE1_DURATION_MS + PHASE2_DURATION_MS + PHASE3_DURATION_MS;

    public PixEventGenerator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedDelayString = "${publisher.simulador.tick-ms:100}")
    public void gerarEventosPix() {
        if (!simuladorEnabled) {
            return;
        }

        // Inicializar tempo de início na primeira execução
        if (startTime.get() == 0) {
            startTime.set(System.currentTimeMillis());
            log.info("Iniciando teste de performance: 2k/s (60s) -> 7k/s (15s) -> 2k/s (60s)");
        }

        long elapsed = System.currentTimeMillis() - startTime.get();
        
        // Verificar se o teste terminou
        if (elapsed >= TOTAL_DURATION_MS) {
            log.info("Teste de performance concluído após {} segundos", elapsed / 1000);
            simuladorEnabled = false;
            return;
        }

        // Calcular taxa atual baseada na fase
        int currentRate = calculateCurrentRate(elapsed);
        int eventosPorTick = calculateEventsPerTick(currentRate, tickMs);

        try {
            PixWebhookBatchRequest batch = gerarLoteEventos(eventosPorTick);
            
            // Criar cobranças para cada evento antes de enviar (se habilitado)
            if (criarCobranca) {
                for (var evento : batch.pix()) {
                    criarCobranca(evento);
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PixWebhookBatchRequest> request = new HttpEntity<>(batch, headers);

            String url = "http://localhost:" + serverPort + "/api/v1/webhooks/pix";
            restTemplate.postForEntity(url, request, Void.class);

            log.info("Fase: {} | Taxa: {} eventos/s | Gerados {} eventos | Tempo decorrido: {}s", 
                getCurrentPhase(elapsed), currentRate, eventosPorTick, elapsed / 1000);
        } catch (Exception e) {
            log.error("Erro ao gerar eventos Pix simulados via HTTP", e);
        }
    }

    private int calculateCurrentRate(long elapsedMs) {
        if (elapsedMs < PHASE1_DURATION_MS) {
            // Fase 1: Taxa base (2k/s)
            return BASE_RATE;
        } else if (elapsedMs < PHASE1_DURATION_MS + PHASE2_DURATION_MS) {
            // Fase 2: Pico (7k/s)
            return PEAK_RATE;
        } else {
            // Fase 3: Taxa base novamente (2k/s)
            return BASE_RATE;
        }
    }

    private int calculateEventsPerTick(int ratePerSecond, int tickMs) {
        return (int) Math.ceil(ratePerSecond * (tickMs / 1000.0));
    }

    private String getCurrentPhase(long elapsedMs) {
        if (elapsedMs < PHASE1_DURATION_MS) {
            return "BASE (2k/s)";
        } else if (elapsedMs < PHASE1_DURATION_MS + PHASE2_DURATION_MS) {
            return "PICO (7k/s)";
        } else {
            return "BASE (2k/s)";
        }
    }

    private void criarCobranca(PixReceivedEventRequest evento) {
        long start = System.currentTimeMillis();
        try {
            String cnpjCliente = gerarCnpj();
            String cpfCnpjDevedor = gerarCpfCnpjDevedor();
            
            CobrancaRequest cobranca = new CobrancaRequest(
                evento.txid(),
                new BigDecimal(evento.valor()),
                evento.chave(),
                cnpjCliente,
                cpfCnpjDevedor,
                " "
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CobrancaRequest> request = new HttpEntity<>(cobranca, headers);

            String url = cobrancaServiceUrl + "/api/v1/cobrancas";
            restTemplate.postForEntity(url, request, Void.class);

            long duration = System.currentTimeMillis() - start;
            log.debug("Cobrança criada para txid: {} em {}ms", evento.txid(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("Erro ao criar cobrança para txid: {} após {}ms", evento.txid(), duration, e);
        }
    }

    private String gerarCnpj() {
        return String.format("%014d", random.nextLong(10000000000000L, 99999999999999L));
    }

    private String gerarCpfCnpjDevedor() {
        // Alterna entre CPF (11 dígitos) e CNPJ (14 dígitos)
        if (random.nextBoolean()) {
            return String.format("%011d", random.nextLong(10000000000L, 99999999999L));
        } else {
            return String.format("%014d", random.nextLong(10000000000000L, 99999999999999L));
        }
    }

    private PixWebhookBatchRequest gerarLoteEventos(int quantidade) {
        java.util.List<PixReceivedEventRequest> eventos = new java.util.ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            eventos.add(gerarEventoSimulado());
        }
        return new PixWebhookBatchRequest(eventos);
    }

    private PixReceivedEventRequest gerarEventoSimulado() {
        String endToEndId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        String txid = UUID.randomUUID().toString().replace("-", "");
        String valor = String.format("%d.%02d", random.nextInt(10000), random.nextInt(100));
        Instant horario = Instant.now();
        String chave = gerarChavePixValida();
        String infoPagador = random.nextBoolean() ? "Pagador teste " + random.nextInt(1000) : null;

        return new PixReceivedEventRequest(endToEndId, txid, valor, horario, chave, infoPagador);
    }

    private String gerarChavePixValida() {
        // Gera chaves Pix válidas: CPF (11 dígitos), CNPJ (14 dígitos), E-mail, Celular, EVP
        int tipo = random.nextInt(5);
        
        switch (tipo) {
            case 0: // CPF
                return String.format("%011d", random.nextLong(10000000000L, 99999999999L));
            case 1: // CNPJ
                return String.format("%014d", random.nextLong(10000000000000L, 99999999999999L));
            case 2: // E-mail
                return "teste" + random.nextInt(10000) + "@exemplo.com";
            case 3: // Celular
                return "+55" + String.format("%011d", random.nextLong(10000000000L, 99999999999L));
            case 4: // EVP (chave aleatória - formato UUID com hífens)
                return UUID.randomUUID().toString();
            default:
                return String.format("%011d", random.nextLong(10000000000L, 99999999999L));
        }
    }
}
