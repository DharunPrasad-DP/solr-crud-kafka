package com.product.catalog.index.service.service;

import com.product.catalog.index.service.model.Product;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SolrCrudService {
    void addProduct(Product product) throws SolrServerException, IOException;

    Product getProductById(String id) throws SolrServerException, IOException;

    boolean deleteProductById(String id) throws SolrServerException, IOException;

    Product atomicUpdateProduct(String id, Map<String, Object> updates) throws SolrServerException, IOException;
}
