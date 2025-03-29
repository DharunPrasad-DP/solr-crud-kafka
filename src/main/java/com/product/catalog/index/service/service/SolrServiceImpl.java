package com.product.catalog.index.service.service;

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
    public void addProduct(Product product) throws SolrServerException, IOException {
        product.setLastUpdatedTime(System.currentTimeMillis());
        solrClient.addBean(collection, product);
        solrClient.commit(collection);
    }

    @Override
    public Product getProductById(String id) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("id:" + id);
        QueryResponse response = solrClient.query(collection, query);
        List<Product> products = response.getBeans(Product.class);
        return products.isEmpty() ? null : products.get(0);
    }

    @Override
    public boolean deleteProductById(String id) throws SolrServerException, IOException {
        if (getProductById(id) != null) {
            solrClient.deleteById(collection, id);
            solrClient.commit(collection);
            return true;
        }
        return false;
    }

    @Override
    public Product atomicUpdateProduct(String id, Map<String, Object> updates) throws SolrServerException, IOException {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.addField("id", id); // Primary key

        Map<String, Object> fieldUpdate;
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            fieldUpdate = new HashMap<>();
            fieldUpdate.put("set", entry.getValue()); // Atomic update operation
            solrDoc.addField(entry.getKey(), fieldUpdate);
        }

        fieldUpdate = new HashMap<>();
        fieldUpdate.put("set", System.currentTimeMillis());
        solrDoc.addField("lastUpdatedTime", fieldUpdate);

        solrClient.add(collection, solrDoc);
        solrClient.commit(collection);

        return getProductById(id);
    }
}
