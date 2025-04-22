package com.example.duangiatsay.controller;

import com.example.duangiatsay.model.LaundryItem;
import com.example.duangiatsay.service.ILaundryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/laundry-items")
@CrossOrigin(origins = "*")
public class LaundryItemController {

    @Autowired
    private ILaundryItemService laundryItemService;

    @PostMapping
    public ResponseEntity<LaundryItem> create(@RequestBody LaundryItem item) {
        return ResponseEntity.ok(laundryItemService.save(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaundryItem> update(@PathVariable Long id, @RequestBody LaundryItem item) {
        LaundryItem updated = laundryItemService.update(id, item);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<LaundryItem>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(laundryItemService.getAllByOrderId(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaundryItem> getById(@PathVariable Long id) {
        LaundryItem item = laundryItemService.getById(id);
        if (item != null) {
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laundryItemService.delete(id);
        return ResponseEntity.ok().build();
    }
}
