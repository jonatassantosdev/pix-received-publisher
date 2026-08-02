package com.itau.pix_received_publisher.core.application.ports.output.messaging;

public interface MessageBrokerPort {
    void publish(String topic, String key, String payload);
}
