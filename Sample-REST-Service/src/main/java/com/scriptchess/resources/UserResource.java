package com.scriptchess.resources;

import com.scriptchess.models.User;
import com.scriptchess.services.EComService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserResource {

    @Autowired
    private EComService service;

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id")int id) {
        return service.getUser(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable("id")int id, @RequestBody User product) {
        return service.updateUser(product, id);
    }

    @PostMapping("/")
    public User createUser(@RequestBody User product) {
        return service.createUser(product);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id")int id) {
        service.deleteUser(id);
    }
}
