package com.itau.pix_received_publisher.infrastructure.adapters.output.persistence.jpa;

import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepositoryJpa extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.status = :status ORDER BY e.criadoEm ASC")
    List<OutboxEventJpaEntity> findByStatusOrderByCriadoEmAsc(OutboxStatus status);

    long countByStatus(OutboxStatus status);
}