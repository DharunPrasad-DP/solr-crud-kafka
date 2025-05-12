package com.product.catalog.index.service.service;

import com.product.catalog.index.service.exception.ResourceNotFoundException;
import com.product.catalog.index.service.model.Product;
import java.util.Map;

public interface SolrCrudService {
    void addProduct(Product product);

    Product getProductById(String id) throws ResourceNotFoundException;

    boolean deleteProductById(String id) throws ResourceNotFoundException;

    Product atomicUpdateProduct(String id, Map<String, Object> updates) throws ResourceNotFoundException;
}
