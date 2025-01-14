package com.scriptchess.models;

import lombok.Data;

import java.util.Date;

@Data
public class Shipment {
    private int id;
    private Order order;
    private SHIPMENT_STATUS status;
    private Date expectedDeliveryDate;
    private Vendor shipmentVendor;
    private String trackingUrl;
}
