package com.example.duangiatsay.controller;

import com.example.duangiatsay.model.Account;
import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.model.OrderStatus;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.AccountRepository;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.service.ILaundryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/shipper/{shipperId}")
    public ResponseEntity<List<LaundryOrder>> getOrdersByShipperId(@PathVariable Long shipperId) {
        return ResponseEntity.ok(orderService.getOrdersByShipperId(shipperId));
    }

    @GetMapping("/shipper/{shipperId}/status")
    public ResponseEntity<List<LaundryOrder>> getOrdersByShipperAndStatus(
            @PathVariable Long shipperId,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.getOrdersByShipperIdAndStatus(shipperId, status));
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeleteOrderByShipper(@PathVariable Long id) {
        try {
            orderService.softDeleteOrderByShipper(id);
            return ResponseEntity.ok("✅ Đơn hàng đã được ẩn khỏi danh sách shipper!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Không thể xoá mềm đơn hàng: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody LaundryOrder order) {
        if (order.getUser() != null && order.getUser().getId() != null) {
            User user = userRepository.findById(order.getUser().getId()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy người dùng với ID: " + order.getUser().getId());
            }
            order.setUser(user);
        }

        if (order.getShipper() != null && order.getShipper().getId() != null) {
            Account shipper = accountRepository.findById(order.getShipper().getId()).orElse(null);
            if (shipper == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy shipper với ID: " + order.getShipper().getId());
            }
            order.setShipper(shipper);
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        } else {
            try {
                order.setStatus(OrderStatus.fromValue(order.getStatus().toString()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("❌ Trạng thái không hợp lệ: " + order.getStatus());
            }
        }

        LaundryOrder createdOrder = orderService.createOrder(order);

        messagingTemplate.convertAndSend(
                "/topic/booking/" + createdOrder.getId(),
                Map.of(
                        "orderId", createdOrder.getId(),
                        "message", "✅ Đơn hàng #" + createdOrder.getId() + " đã được đặt thành công!"
                )
        );

        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{id}/soft-delete-admin")
    public ResponseEntity<?> softDeleteOrderByAdmin(@PathVariable Long id) {
        try {
            orderService.softDeleteOrderByAdmin(id);
            return ResponseEntity.ok("✅ Đơn hàng đã bị ẩn khỏi danh sách admin!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Không thể xoá mềm: " + e.getMessage());
        }
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
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ Trạng thái không hợp lệ: " + status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
