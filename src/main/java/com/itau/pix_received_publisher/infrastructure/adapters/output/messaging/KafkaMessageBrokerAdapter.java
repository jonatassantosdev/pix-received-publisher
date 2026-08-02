package com.itau.pix_received_publisher.infrastructure.adapters.output.messaging;

import com.itau.pix_received_publisher.core.application.ports.output.messaging.MessageBrokerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageBrokerAdapter implements MessageBrokerPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessageBrokerAdapter(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String key, String payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}
