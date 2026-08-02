package com.itau.pix_received_publisher.core.application.ports.output.services;

import com.itau.pix_received_publisher.core.domain.models.PixReceivedEvent;

public interface EventValidatorPort {
    boolean isValid(PixReceivedEvent event);
}
