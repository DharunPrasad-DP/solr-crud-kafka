package com.product.catalog.index.service.controller;

import com.product.catalog.index.service.model.Product;
import com.product.catalog.index.service.model.ProductEvent;
import com.product.catalog.index.service.service.KafkaProducerService;
import com.product.catalog.index.service.service.SolrCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolrCrudControllerTest {

    @Mock
    private SolrCrudService solrService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private SolrCrudController solrCrudController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void publishEvent_ShouldReturnOkResponse() {
        ProductEvent event = new ProductEvent("CREATE", new Product());
        doNothing().when(kafkaProducerService).sendMessage(event);
        ResponseEntity<ProductEvent> response = solrCrudController.publishEvent(event);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());
        verify(kafkaProducerService, times(1)).sendMessage(event);
    }

    @Test
    void addProduct_ShouldReturnCreatedResponse() {
        Product product = new Product();
        product.setId("123");
        product.setName("Test Product");
        product.setPrice(100.0);
        doNothing().when(solrService).addProduct(any(Product.class));
        ResponseEntity<Product> response = solrCrudController.addProduct(product);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(solrService, times(1)).addProduct(product);
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        String id = "123";
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        when(solrService.getProductById(id)).thenReturn(product);
        ResponseEntity<Product> response = solrCrudController.getProductById(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getId(), response.getBody().getId());
        assertEquals(product.getName(), response.getBody().getName());
        verify(solrService, times(1)).getProductById(id);
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() {
        String id = "123";
        when(solrService.deleteProductById(id)).thenReturn(true); // Mock the return value
        ResponseEntity<Void> response = solrCrudController.deleteProduct(id);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(solrService, times(1)).deleteProductById(id);
    }

    @Test
    void atomicUpdateProduct_ShouldReturnUpdatedProduct() {
        String id = "123";
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Product");
        Product updatedProduct = new Product();
        updatedProduct.setId(id);
        updatedProduct.setName("Updated Product");
        when(solrService.atomicUpdateProduct(id, updates)).thenReturn(updatedProduct);
        ResponseEntity<Product> response = solrCrudController.atomicUpdateProduct(id, updates);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedProduct.getId(), response.getBody().getId());
        assertEquals(updatedProduct.getName(), response.getBody().getName());
        verify(solrService, times(1)).atomicUpdateProduct(id, updates);
    }
} 