package com.product.catalog.index.service.service;

import com.product.catalog.index.service.model.ProductEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public KafkaProducerService(KafkaTemplate<String, ProductEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ProductEvent event) {
        kafkaTemplate.send(topicName, event);
    }
}

