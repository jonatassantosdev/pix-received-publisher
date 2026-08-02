package com.itau.pix_received_publisher.core.application.mappers;

import com.itau.pix_received_publisher.core.domain.entities.OutboxEvent;
import com.itau.pix_received_publisher.infrastructure.adapters.output.persistence.jpa.OutboxEventJpaEntity;

public class OutboxEventMapper {

    public static OutboxEventJpaEntity toJpa(OutboxEvent domain) {
        OutboxEventJpaEntity jpa = new OutboxEventJpaEntity();
        jpa.setId(domain.getId());
        jpa.setTopico(domain.getTopico());
        jpa.setChaveParticao(domain.getChaveParticao());
        jpa.setPayload(domain.getPayload());
        jpa.setStatus(domain.getStatus());
        jpa.setCriadoEm(domain.getCriadoEm());
        jpa.setTentativas(domain.getTentativas());
        jpa.setPublicadoEm(domain.getPublicadoEm());
        return jpa;
    }

    public static OutboxEvent toDomain(OutboxEventJpaEntity jpa) {
        OutboxEvent domain = new OutboxEvent();
        domain.setId(jpa.getId());
        domain.setTopico(jpa.getTopico());
        domain.setChaveParticao(jpa.getChaveParticao());
        domain.setPayload(jpa.getPayload());
        domain.setStatus(jpa.getStatus());
        domain.setCriadoEm(jpa.getCriadoEm());
        domain.setTentativas(jpa.getTentativas());
        domain.setPublicadoEm(jpa.getPublicadoEm());
        return domain;
    }
}
