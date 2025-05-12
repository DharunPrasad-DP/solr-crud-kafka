package com.product.catalog.index.service.controller;

import com.product.catalog.index.service.model.Product;
import com.product.catalog.index.service.model.ProductEvent;
import com.product.catalog.index.service.service.KafkaProducerService;
import com.product.catalog.index.service.service.SolrCrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.base.path}")
public class SolrCrudController {

    @Autowired
    private SolrCrudService solrService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostMapping("/publishKafkaEventToSolr")
    public ResponseEntity<ProductEvent> publishEvent(@RequestBody ProductEvent event) {
        kafkaProducerService.sendMessage(event);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/addProductToSolr")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        solrService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/updateDetailsOfProduct/{id}")
    public ResponseEntity<Product> atomicUpdateProduct(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        Product updatedProduct = solrService.atomicUpdateProduct(id, updates);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/getProductById/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = solrService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/deleteProductById/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        solrService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
