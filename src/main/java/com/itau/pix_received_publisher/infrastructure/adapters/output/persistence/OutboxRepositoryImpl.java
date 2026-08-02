package com.itau.pix_received_publisher.infrastructure.adapters.output.persistence;

import com.itau.pix_received_publisher.core.application.mappers.OutboxEventMapper;
import com.itau.pix_received_publisher.core.application.ports.output.repositories.OutboxRepository;
import com.itau.pix_received_publisher.core.domain.entities.OutboxEvent;
import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;
import com.itau.pix_received_publisher.infrastructure.adapters.output.persistence.jpa.OutboxEventJpaEntity;
import com.itau.pix_received_publisher.infrastructure.adapters.output.persistence.jpa.OutboxRepositoryJpa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxRepositoryJpa jpaRepository;

    public OutboxRepositoryImpl(OutboxRepositoryJpa jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public OutboxEvent save(OutboxEvent outboxEvent) {
        OutboxEventJpaEntity jpaEntity = OutboxEventMapper.toJpa(outboxEvent);
        OutboxEventJpaEntity saved = jpaRepository.save(jpaEntity);
        return OutboxEventMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OutboxEvent> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(OutboxEventMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findByStatus(OutboxStatus status, int limit) {
        List<OutboxEventJpaEntity> entities = jpaRepository.findByStatusOrderByCriadoEmAsc(status);
        return entities.stream()
                .limit(limit)
                .map(OutboxEventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OutboxStatus status) {
        return jpaRepository.countByStatus(status);
    }
}
