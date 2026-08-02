package com.itau.pix_received_publisher.core.application.ports.output.repositories;

import com.itau.pix_received_publisher.core.domain.entities.OutboxEvent;
import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository {
    OutboxEvent save(OutboxEvent outboxEvent);
    Optional<OutboxEvent> findById(java.util.UUID id);
    List<OutboxEvent> findByStatus(OutboxStatus status, int limit);
    long countByStatus(OutboxStatus status);
}
