package com.example.duangiatsay.controller;

import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.repository.AccountRepository;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.service.ILaundryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class LaundryOrderController {

    @Autowired
    private ILaundryOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping
    public ResponseEntity<LaundryOrder> createOrder(@RequestBody LaundryOrder order) {
        if (order.getUser() != null && order.getUser().getId() != null) {
            order.setUser(userRepository.findById(order.getUser().getId()).orElse(null));
        }
        if (order.getShipper() != null && order.getShipper().getId() != null) {
            order.setShipper(accountRepository.findById(order.getShipper().getId()).orElse(null));
        }
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping
    public ResponseEntity<List<LaundryOrder>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaundryOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LaundryOrder>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LaundryOrder> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
