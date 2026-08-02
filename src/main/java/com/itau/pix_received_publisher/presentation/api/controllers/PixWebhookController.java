package com.itau.pix_received_publisher.presentation.api.controllers;

import com.itau.pix_received_publisher.core.application.dtos.PixWebhookBatchRequest;
import com.itau.pix_received_publisher.core.application.usecases.ProcessPixEventUseCase;
import com.itau.pix_received_publisher.presentation.api.presenters.SuccessResponsePresenter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class PixWebhookController {

    private final ProcessPixEventUseCase processPixEventUseCase;
    private final Counter eventsReceivedCounter;

    public PixWebhookController(ProcessPixEventUseCase processPixEventUseCase, MeterRegistry meterRegistry) {
        this.processPixEventUseCase = processPixEventUseCase;
        this.eventsReceivedCounter = Counter.builder("pix.publisher.events.received")
                .description("Volume de eventos recebidos via webhook")
                .register(meterRegistry);
    }

    @PostMapping("/pix")
    public ResponseEntity<SuccessResponsePresenter> receberPix(@RequestBody PixWebhookBatchRequest batch) {
        int processedCount = processPixEventUseCase.executeBatch(batch);
        eventsReceivedCounter.increment(processedCount);
        return ResponseEntity.ok(new SuccessResponsePresenter());
    }
}
