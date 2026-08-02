package com.itau.pix_received_publisher.infrastructure.adapters.output.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.pix_received_publisher.core.domain.models.PixReceivedEvent;
import com.itau.pix_received_publisher.core.application.ports.output.services.EventSerializerPort;
import org.springframework.stereotype.Component;

@Component
public class JacksonEventSerializerAdapter implements EventSerializerPort {

    private final ObjectMapper objectMapper;

    public JacksonEventSerializerAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(PixReceivedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
