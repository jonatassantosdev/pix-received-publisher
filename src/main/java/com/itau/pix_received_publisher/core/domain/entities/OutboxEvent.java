package com.itau.pix_received_publisher.core.domain.entities;

import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;

import java.time.Instant;
import java.util.UUID;

public class OutboxEvent {
    private UUID id;
    private String topico;
    private String chaveParticao;
    private String payload;
    private OutboxStatus status;
    private Instant criadoEm;
    private int tentativas;
    private Instant publicadoEm;

    public OutboxEvent() {
    }

    public OutboxEvent(String topico, String chaveParticao, String payload) {
        this.topico = topico;
        this.chaveParticao = chaveParticao;
        this.payload = payload;
        this.status = OutboxStatus.PENDENTE;
        this.criadoEm = Instant.now();
        this.tentativas = 0;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTopico() {
        return topico;
    }

    public void setTopico(String topico) {
        this.topico = topico;
    }

    public String getChaveParticao() {
        return chaveParticao;
    }

    public void setChaveParticao(String chaveParticao) {
        this.chaveParticao = chaveParticao;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public Instant getPublicadoEm() {
        return publicadoEm;
    }

    public void setPublicadoEm(Instant publicadoEm) {
        this.publicadoEm = publicadoEm;
    }
}
