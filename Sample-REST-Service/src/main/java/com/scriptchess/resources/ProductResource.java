package com.scriptchess.resources;

import com.scriptchess.models.Product;
import com.scriptchess.services.EComService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("products")
public class ProductResource {

    @Autowired
    private EComService service;

    @GetMapping
    public List<Product> getAllProducts(@RequestParam("page") int page, @RequestParam("count") int count) {
        return service.getProducts(page, count);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id")int id) {
        return service.getProduct(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable("id")int id, @RequestBody Product product) {
        return service.updateProduct(product, id);
    }

    @PostMapping("/")
    public Product createProduct(@RequestBody Product product) {
        return service.createProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id")int id) {
        service.deleteProduct(id);
    }

}
