package com.product.catalog.index.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {
    private String operation; // CREATE, UPDATE, DELETE
    private Product product;
}
