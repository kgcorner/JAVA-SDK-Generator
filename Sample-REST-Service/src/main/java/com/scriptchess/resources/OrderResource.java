package com.scriptchess.resources;

import com.scriptchess.models.Order;
import com.scriptchess.services.EComService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("orders")
public class OrderResource {
    @Autowired
    private EComService service;

    @GetMapping    public List<Order> getAllOrders(@RequestParam("page") int page, @RequestParam("count") int count
            , @RequestParam("userId") int userId) {
        return service.getOrders(userId, page, count);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id")int id) {
        return service.getOrder(id);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable("id")int id, @RequestBody Order order) {
        return service.updateOrder(order, id);
    }

    @PostMapping("/")
    public Order createOrder(@RequestBody Order order) {
        return service.createOrder(order);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable("id")int id) {
        service.deleteOrder(id);
    }
}
