package com.product.catalog.index.service.exception;

//This exception wraps another exception (e.g., an IOException or SolrServerException).
public class SolrOperationException extends RuntimeException {
    public SolrOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 