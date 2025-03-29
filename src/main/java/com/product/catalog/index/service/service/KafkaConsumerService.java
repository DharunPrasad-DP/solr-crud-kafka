package com.product.catalog.index.service.service;

import com.product.catalog.index.service.model.Product;
import com.product.catalog.index.service.model.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaConsumerService {
    @Autowired
    SolrCrudService solrService;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "product-group")
    public void consume(ProductEvent event) throws SolrServerException, IOException {
        log.info("Received Kafka event: {}", event);

        switch (event.getOperation()) {
            case "CREATE":
                solrService.addProduct(event.getProduct());
                break;
            case "UPDATE":
                Map<String, Object> updates = extractUpdatedFields(event.getProduct());
                solrService.atomicUpdateProduct(event.getProduct().getId(), updates);
                break;
            case "DELETE":
                solrService.deleteProductById(event.getProduct().getId());
                break;
            default:
                log.warn("Unknown operation: {}", event.getOperation());
        }
    }

    private Map<String, Object> extractUpdatedFields(Product product) {
        Map<String, Object> updates = new HashMap<>();

        if (product.getName() != null && !product.getName().isEmpty()) {
            updates.put("name", product.getName());
        }
        if (product.getPrice() != null && product.getPrice()!=0.0) {
            updates.put("price", product.getPrice());
        }
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            updates.put("category", product.getCategory());
        }
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            updates.put("description", product.getDescription());
        }
        return updates;
    }

}


