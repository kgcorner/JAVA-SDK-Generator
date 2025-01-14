package com.scriptchess.resources;

import com.scriptchess.models.Vendor;
import org.springframework.web.bind.annotation.RestController;
import com.scriptchess.models.Product;
import com.scriptchess.services.EComService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("vendors")
public class VendorResource {

    @Autowired
    private EComService service;


    @GetMapping("/{id}")
    public Vendor getVendor(@PathVariable("id")int id) {
        return service.getVendor(id);
    }

    @PutMapping("/{id}")
    public Vendor updateVendor(@PathVariable("id")int id, @RequestBody Vendor product) {
        return service.updateVendor(product, id);
    }

    @PostMapping("/")
    public Vendor createVendor(@RequestBody Vendor product) {
        return service.createVendor(product);
    }

    @DeleteMapping("/{id}")
    public void deleteVendor(@PathVariable("id")int id) {
        service.deleteVendor(id);
    }

}
