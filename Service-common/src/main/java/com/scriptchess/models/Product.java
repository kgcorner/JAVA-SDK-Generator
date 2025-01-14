package com.scriptchess.models;

import lombok.Data;

@Data
public class Product {
    private int id;
    private String name;
    private String description;
    private double basePrice;
    private double appliedDiscount;
    private ProductCategory category;
}
