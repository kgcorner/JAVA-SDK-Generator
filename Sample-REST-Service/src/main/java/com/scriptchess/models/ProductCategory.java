package com.scriptchess.models;

import lombok.Data;

import java.util.List;

@Data
public class ProductCategory {
    private int id;
    private String name;
    private String description;
    private List<ProductCategory> childCategory;
}
