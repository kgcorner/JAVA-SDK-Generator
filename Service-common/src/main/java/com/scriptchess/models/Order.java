package com.scriptchess.models;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Order {
    private int id;
    private List<Product> products;
    private Date dateOfOrder;
    private double totalPrice;
    private double totalBasePrice;
    private User user;
    private Address shipTo;
    private boolean returned;
    private boolean cancelled;
}
