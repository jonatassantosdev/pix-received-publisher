package com.itau.pix_received_publisher.infrastructure.adapters.output.persistence.jpa;

import com.itau.pix_received_publisher.core.domain.enums.OutboxStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "pix_received_publisher")
public class OutboxEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String topico;

    @Column(nullable = false, length = 64)
    private String chaveParticao;

    @Column(nullable = false, length = 5000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int tentativas;

    @Column(nullable = false)
    private Instant criadoEm;

    private Instant publicadoEm;

    public OutboxEventJpaEntity() {}

    public OutboxEventJpaEntity(UUID id, String topico, String chaveParticao, String payload, OutboxStatus status, int tentativas, Instant criadoEm, Instant publicadoEm) {
        this.id = id;
        this.topico = topico;
        this.chaveParticao = chaveParticao;
        this.payload = payload;
        this.status = status;
        this.tentativas = tentativas;
        this.criadoEm = criadoEm;
        this.publicadoEm = publicadoEm;
    }

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = Instant.now();
        }
        if (this.status == null) {
            this.status = OutboxStatus.PENDENTE;
        }
        if (this.tentativas == 0) {
            this.tentativas = 0;
        }
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

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getPublicadoEm() {
        return publicadoEm;
    }

    public void setPublicadoEm(Instant publicadoEm) {
        this.publicadoEm = publicadoEm;
    }
}
