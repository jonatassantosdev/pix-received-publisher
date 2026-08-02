package com.itau.pix_received_publisher.infrastructure.schedulers;

import java.math.BigDecimal;

public record CobrancaRequest(
    String txid,
    BigDecimal valorOriginal,
    String chave,
    String cnpjCliente,
    String cpfCnpjDevedor,
    String descricao
) {}
