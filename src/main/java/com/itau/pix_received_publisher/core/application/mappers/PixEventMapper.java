package com.itau.pix_received_publisher.core.application.mappers;

import com.itau.pix_received_publisher.core.application.dtos.PixReceivedEventRequest;
import com.itau.pix_received_publisher.core.domain.models.PixReceivedEvent;

public class PixEventMapper {

    public static PixReceivedEvent toDomain(PixReceivedEventRequest request) {
        return new PixReceivedEvent(
            request.endToEndId(),
            request.txid(),
            request.valor(),
            request.horario(),
            request.chave(),
            request.infoPagador()
        );
    }
}
