package com.product.catalog.index.service.controller;

import com.product.catalog.index.service.model.Product;
import com.product.catalog.index.service.model.ProductEvent;
import com.product.catalog.index.service.service.KafkaProducerService;
import com.product.catalog.index.service.service.SolrCrudService;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("${api.base.path}")
public class SolrCrudController {

    @Autowired
    private SolrCrudService solrService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostMapping("/publishKafkaEventToSolr")
    public ResponseEntity<String> publishEvent(@RequestBody ProductEvent event) {
        kafkaProducerService.sendMessage(event);
        return ResponseEntity.ok("Event Published: " + event);
    }

    @PostMapping("/indexProductToSolr")
    public ResponseEntity<String> addProduct(@RequestBody Product product) throws Exception {
        try {
            solrService.addProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body("Product added successfully!");
        } catch (SolrServerException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding product: " + e.getMessage());
        }
    }

    //    no proper error handling (returns null)
    @PatchMapping("updateDetailsOfProduct/{id}")
    public ResponseEntity<Product> atomicUpdateProduct(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) throws SolrServerException, IOException {
        return new ResponseEntity<>(solrService.atomicUpdateProduct(id, updates), HttpStatus.OK);
    }

    //    no proper error handling (returns null)
    @GetMapping("getProductById/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) throws Exception {
        if (solrService.getProductById(id) != null) {
            return new ResponseEntity<>(solrService.getProductById(id), HttpStatus.OK);
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @DeleteMapping("deleteProductById/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) throws Exception {
        if (solrService.deleteProductById(id)) {
            return new ResponseEntity<>("Product details deleted from Solr", HttpStatus.OK);
        }
        return new ResponseEntity<>("Details are not deleted for the given productId :: " + id, HttpStatus.NOT_FOUND);
    }
}
