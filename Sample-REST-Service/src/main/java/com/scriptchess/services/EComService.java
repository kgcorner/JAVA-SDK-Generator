package com.scriptchess.services;

import com.scriptchess.exception.ResourceNotFound;
import com.scriptchess.models.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EComService {
    private Map<Integer, Product> productsMap = new HashMap<>();
    private Map<Integer, ProductCategory> categoryMap = new HashMap<>();
    private Map<Integer, Order> orderMap = new HashMap<>();
    private Map<Integer, Shipment> shipmentMap = new HashMap<>();
    private Map<Integer, User> userMap = new HashMap<>();
    private Map<Integer, Vendor> vendorMap = new HashMap<>();
    private static long GLOBAL_INDEX= 0 ;

    public List<Product> getProducts(int page, int count) {
        List<Product> products = new ArrayList<>();
        int offset = (page -1) * count;
        int index = 0;
        if(productsMap.size() > offset) {
            for(Map.Entry<Integer, Product> entry : productsMap.entrySet()) {
                if(index > offset) {
                    products.add(entry.getValue());
                }
                index++;
            }
        }
        return products;
    }

    public Product getProduct(int productId) {
        for(Map.Entry<Integer, Product> entry : productsMap.entrySet()) {
            if(entry.getKey() == productId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Product createProduct(Product product) {
        if(product.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in product while creating it");
        }
        product.setId(getId());
        productsMap.put(product.getId(), product);
        return product;
    }

    public Product updateProduct(Product product, int productId) {
        Product existingProduct = getProduct(productId);
        if(existingProduct == null)
            throw new ResourceNotFound("Product doesn't exists");
        BeanUtils.copyProperties(product, existingProduct);
        existingProduct.setId(productId);
        productsMap.put(product.getId(), product);
        return product;
    }

    public void deleteProduct(int productId) {
        Product existingProduct = getProduct(productId);
        if(existingProduct == null)
            throw new ResourceNotFound("Product doesn't exists");
        productsMap.remove(productId);
    }




    public List<Order> getOrders(int userId, int page, int count) {
        List<Order> orders = new ArrayList<>();
        int offset = (page -1) * count;
        int index = 0;
        List<Order> ordersOfUser = new ArrayList<>();
        for(Map.Entry<Integer, Order> entry : orderMap.entrySet()) {
            if(entry.getValue().getUser().getId() == userId) {
                ordersOfUser.add(entry.getValue());
            }
        }
        if(ordersOfUser.size() > offset) {
            for (int i = offset; i < ordersOfUser.size(); i++) {
                orders.add(ordersOfUser.get(i));
            }
        }
        return orders;
    }

    public Order getOrder(int orderId) {
        for(Map.Entry<Integer, Order> entry : orderMap.entrySet()) {
            if(entry.getKey() == orderId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Order createOrder(Order order) {
        if(order.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in order while creating it");
        }
        order.setId(getId());
        orderMap.put(order.getId(), order);
        return order;
    }

    public Order updateOrder(Order order, int orderId) {
        Order existingOrder = getOrder(orderId);
        if(existingOrder == null)
            throw new ResourceNotFound("Order doesn't exists");
        BeanUtils.copyProperties(order, existingOrder);
        existingOrder.setId(orderId);
        orderMap.put(order.getId(), order);
        return order;
    }

    public void deleteOrder(int orderId) {
        Order existingOrder = getOrder(orderId);
        if(existingOrder == null)
            throw new ResourceNotFound("Order doesn't exists");
        orderMap.remove(orderId);
    }

    public List<ProductCategory> getCategories() {
        List<ProductCategory> categories = new ArrayList<>();
        for(Map.Entry<Integer, ProductCategory> entry : categoryMap.entrySet()) {
            categories.add(entry.getValue());
        }
        return categories;
    }

    public ProductCategory getCategory(int categoryId) {
        for(Map.Entry<Integer, ProductCategory> entry : categoryMap.entrySet()) {
            if(entry.getKey() == categoryId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public ProductCategory createCategory(ProductCategory category) {
        if(category.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in category while creating it");
        }
        category.setId(getId());
        categoryMap.put(category.getId(), category);
        return category;
    }

    public ProductCategory updateCategory(ProductCategory category, int categoryId) {
        ProductCategory existingCategory = getCategory(categoryId);
        if(existingCategory == null)
            throw new ResourceNotFound("Category doesn't exists");
        BeanUtils.copyProperties(category, existingCategory);
        existingCategory.setId(categoryId);
        categoryMap.put(category.getId(), category);
        return category;
    }

    public void deleteCategory(int categoryId) {
        ProductCategory existingCategory = getCategory(categoryId);
        if(existingCategory == null)
            throw new ResourceNotFound("Category doesn't exists");
        categoryMap.remove(categoryId);
    }

    public Shipment getShipment(int shipmentId) {
        for(Map.Entry<Integer, Shipment> entry : shipmentMap.entrySet()) {
            if(entry.getKey() == shipmentId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Shipment createShipment(Shipment shipment) {
        if(shipment.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in shipment while creating it");
        }
        shipment.setId(getId());
        shipmentMap.put(shipment.getId(), shipment);
        return shipment;
    }

    public Shipment updateShipment(Shipment shipment, int shipmentId) {
        Shipment existingShipment = getShipment(shipmentId);
        if(existingShipment == null)
            throw new ResourceNotFound("Shipment doesn't exists");
        BeanUtils.copyProperties(shipment, existingShipment);
        existingShipment.setId(shipmentId);
        shipmentMap.put(shipment.getId(), shipment);
        return shipment;
    }

    public void deleteShipment(int shipmentId) {
        Shipment existingShipment = getShipment(shipmentId);
        if(existingShipment == null)
            throw new ResourceNotFound("Shipment doesn't exists");
        shipmentMap.remove(shipmentId);
    }

    public List<Address> getAddress(int usedId) {
        for(Map.Entry<Integer, User> entry : userMap.entrySet()) {
            if(entry.getKey() == usedId) {
                return entry.getValue().getAddresses();
            }
        }
        return Collections.EMPTY_LIST;
    }

    public Address addAddress(int userId, Address address) {
        User user = getUser(userId);
        if(user == null)
            throw new ResourceNotFound("User not found");
        address.setId(getId());
        user.getAddresses().add(address);
        return address;
    }

    public User getUser(int usedId) {
        for(Map.Entry<Integer, User> entry : userMap.entrySet()) {
            if(entry.getKey() == usedId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public User createUser(User user) {
        if(user.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in user while creating it");
        }
        user.setId(getId());
        userMap.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user, int userId) {
        User existingUser = getUser(userId);
        if(existingUser == null)
            throw new ResourceNotFound("User doesn't exists");
        BeanUtils.copyProperties(user, existingUser);
        existingUser.setId(userId);
        userMap.put(user.getId(), user);
        return user;
    }

    public void deleteUser(int userId) {
        User existingUser = getUser(userId);
        if(existingUser == null)
            throw new ResourceNotFound("User doesn't exists");
        userMap.remove(userId);
    }

    public Vendor getVendor(int vendorId) {
        for(Map.Entry<Integer, Vendor> entry : vendorMap.entrySet()) {
            if(entry.getKey() == vendorId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Vendor createVendor(Vendor vendor) {
        if(vendor.getId() > 0) {
            throw new IllegalArgumentException("Id is not expected in vendor while creating it");
        }
        vendor.setId(getId());
        vendorMap.put(vendor.getId(), vendor);
        return vendor;
    }

    public Vendor updateVendor(Vendor vendor, int vendorId) {
        Vendor existingVendor = getVendor(vendorId);
        if(existingVendor == null)
            throw new ResourceNotFound("Vendor doesn't exists");
        BeanUtils.copyProperties(vendor, existingVendor);
        existingVendor.setId(vendorId);
        vendorMap.put(vendor.getId(), vendor);
        return vendor;
    }

    public void deleteVendor(int vendorId) {
        Vendor existingVendor = getVendor(vendorId);
        if(existingVendor == null)
            throw new ResourceNotFound("Vendor doesn't exists");
        vendorMap.remove(vendorId);
    }

    private int getId() {
        Long globalId = GLOBAL_INDEX;
        return globalId.intValue();
    }
}
