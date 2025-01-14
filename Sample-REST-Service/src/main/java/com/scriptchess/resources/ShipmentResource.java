package com.scriptchess.resources;

import com.scriptchess.models.Product;
import com.scriptchess.models.Shipment;
import com.scriptchess.services.EComService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("shipments")
public class ShipmentResource {

    @Autowired
    private EComService service;

    @GetMapping("/{id}")
    public Shipment getShipment(@PathVariable("id")int id) {
        return service.getShipment(id);
    }

    @PutMapping("/{id}")
    public Shipment updateShipment(@PathVariable("id")int id, @RequestBody Shipment product) {
        return service.updateShipment(product, id);
    }

    @PostMapping("/")
    public Shipment createShipment(@RequestBody Shipment product) {
        return service.createShipment(product);
    }

    @DeleteMapping("/{id}")
    public void deleteShipment(@PathVariable("id")int id) {
        service.deleteShipment(id);
    }
}
