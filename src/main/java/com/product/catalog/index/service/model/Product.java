package com.product.catalog.index.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Field("id")  // Primary Key in Solr
    private String id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private Double price;

    @Field("category")
    private String category;

    @Field("lastUpdatedTime")
    private long lastUpdatedTime;
}
