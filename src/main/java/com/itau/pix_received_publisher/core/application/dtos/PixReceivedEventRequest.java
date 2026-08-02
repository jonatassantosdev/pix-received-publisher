package com.itau.pix_received_publisher.core.application.dtos;

import java.time.Instant;

public record PixReceivedEventRequest(
    String endToEndId,
    String txid,
    String valor,
    Instant horario,
    String chave,
    String infoPagador
) {}
