package com.product.catalog.index.service.service;

import com.product.catalog.index.service.exception.ResourceNotFoundException;
import com.product.catalog.index.service.exception.SolrOperationException;
import com.product.catalog.index.service.model.Product;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SolrServiceImplTest {

    @Mock
    private SolrClient solrClient;

    @InjectMocks
    private SolrServiceImpl solrService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testProduct = new Product();
        testProduct.setId("123");
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);
    }

    @Test
    void addProduct_ShouldAddProductToSolr() throws SolrServerException, IOException {
        when(solrClient.addBean(anyString(), any(Product.class))).thenReturn(null); // Mock the return value
        when(solrClient.commit(anyString())).thenReturn(null); // Mock the commit method
        solrService.addProduct(testProduct);
        verify(solrClient, times(1)).addBean(anyString(), eq(testProduct));
        verify(solrClient, times(1)).commit(anyString());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of(testProduct));
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        Product result = solrService.getProductById("123");
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
    }

    @Test
    void getProductById_ShouldThrowResourceNotFoundException_WhenProductNotFound() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of());
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        assertThrows(ResourceNotFoundException.class, () -> solrService.getProductById("123"));
    }

    @Test
    void deleteProductById_ShouldDeleteProduct() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of(testProduct));
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        when(solrClient.deleteById(anyString(), anyString())).thenReturn(null); // Mock deleteById
        when(solrClient.commit(anyString())).thenReturn(null); // Mock commit
        boolean result = solrService.deleteProductById("123");
        assertTrue(result);
        verify(solrClient, times(1)).deleteById(anyString(), eq("123"));
        verify(solrClient, times(1)).commit(anyString());
    }

    @Test
    void deleteProductById_ShouldReturnFalse_WhenProductNotFound() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of());
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        boolean result = solrService.deleteProductById("123");
        assertFalse(result);
        verify(solrClient, never()).deleteById(anyString(), anyString());
    }

    @Test
    void atomicUpdateProduct_ShouldUpdateProduct() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of(testProduct));
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        when(solrClient.add(anyString(), any(SolrInputDocument.class))).thenReturn(null); // Mock the add method
        when(solrClient.commit(anyString())).thenReturn(null); // Mock the commit method
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Product");
        Product result = solrService.atomicUpdateProduct("123", updates);
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        verify(solrClient, times(1)).add(anyString(), any(SolrInputDocument.class));
        verify(solrClient, times(1)).commit(anyString());
    }

    @Test
    void atomicUpdateProduct_ShouldThrowResourceNotFoundException_WhenProductNotFound() throws SolrServerException, IOException {
        QueryResponse mockResponse = mock(QueryResponse.class);
        when(mockResponse.getBeans(Product.class)).thenReturn(List.of());
        when(solrClient.query(anyString(), any(SolrQuery.class))).thenReturn(mockResponse);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Product");
        assertThrows(ResourceNotFoundException.class, () -> solrService.atomicUpdateProduct("123", updates));
    }

    @Test
    void addProduct_ShouldThrowSolrOperationException_WhenSolrServerException() throws SolrServerException, IOException {
        doThrow(new SolrServerException("Test exception")).when(solrClient).addBean(anyString(), any(Product.class));
        assertThrows(SolrOperationException.class, () -> solrService.addProduct(testProduct));
    }
} 