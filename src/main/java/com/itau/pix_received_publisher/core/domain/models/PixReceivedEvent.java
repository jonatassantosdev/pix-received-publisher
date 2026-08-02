package com.itau.pix_received_publisher.core.domain.models;

import java.time.Instant;

public record PixReceivedEvent(
    String endToEndId,
    String txid,
    String valor,
    Instant horario,
    String chave,
    String infoPagador
) {}
