package com.product.catalog.index.service.service;

import com.product.catalog.index.service.exception.ResourceNotFoundException;
import com.product.catalog.index.service.exception.SolrOperationException;
import com.product.catalog.index.service.model.Product;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolrServiceImpl implements SolrCrudService {
    private final String collection = "products_catalog";

    @Autowired
    private SolrClient solrClient;

    @Override
    public void addProduct(Product product) {
        try {
            product.setLastUpdatedTime(System.currentTimeMillis());
            solrClient.addBean(collection, product);
            solrClient.commit(collection);
        } catch (SolrServerException | IOException e) {
            throw new SolrOperationException("Failed to add product", e);
        }
    }

    @Override
    public Product getProductById(String id) throws ResourceNotFoundException {
        try {
            SolrQuery query = new SolrQuery("id:" + id);
            QueryResponse response = solrClient.query(collection, query);
            List<Product> products = response.getBeans(Product.class);
            if (products.isEmpty()) {
                throw new ResourceNotFoundException("Product not found with id: " + id);
            }
            return products.get(0);
        } catch (SolrServerException | IOException e) {
            throw new SolrOperationException("Failed to get product", e);
        }
    }

    @Override
    public boolean deleteProductById(String id) {
        try {
            Product product = getProductById(id); // This may throw ResourceNotFoundException
            solrClient.deleteById("products", id);
            solrClient.commit("products");
            return true;
        } catch (ResourceNotFoundException e) {
            return false; // Return false if the product is not found
        } catch (SolrServerException | IOException e) {
            throw new SolrOperationException("Failed to delete product with id: " + id, e);
        }
    }

    @Override
    public Product atomicUpdateProduct(String id, Map<String, Object> updates) throws ResourceNotFoundException {
        try {
            // First check if product exists
            getProductById(id);

            SolrInputDocument solrDoc = new SolrInputDocument();
            solrDoc.addField("id", id);

            Map<String, Object> fieldUpdate;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                fieldUpdate = new HashMap<>();
                fieldUpdate.put("set", entry.getValue());
                solrDoc.addField(entry.getKey(), fieldUpdate);
            }

            fieldUpdate = new HashMap<>();
            fieldUpdate.put("set", System.currentTimeMillis());
            solrDoc.addField("lastUpdatedTime", fieldUpdate);

            solrClient.add(collection, solrDoc);
            solrClient.commit(collection);

            return getProductById(id);
        } catch (SolrServerException | IOException e) {
            throw new SolrOperationException("Failed to update product", e);
        }
    }
}
