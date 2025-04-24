package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.LaundryItem;
import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.model.OrderStatus;
import com.example.duangiatsay.repository.LaundryOrderRepository;
import com.example.duangiatsay.service.ILaundryOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LaundryOrderService implements ILaundryOrderService {

    @Autowired
    private LaundryOrderRepository orderRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public LaundryOrder createOrder(LaundryOrder order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }

        // ✅ Gán lại order cho từng item
        if (order.getItems() != null) {
            for (LaundryItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        return orderRepository.save(order);
    }


    @Override
    public LaundryOrder getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<LaundryOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<LaundryOrder> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    @Override
    public List<LaundryOrder> getOrdersByShipperId(Long shipperId) {
        return orderRepository.findByShipperId(shipperId);
    }
    @Override
    public void softDeleteOrderByShipper(Long id) {
        LaundryOrder order = getOrderById(id);
        if (order != null) {
            order.setDeletedByShipper(true);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    @Override
    public LaundryOrder updateOrderStatus(Long id, String status) {
        LaundryOrder order = getOrderById(id);
        if (order != null) {
            try {
                OrderStatus newStatus = OrderStatus.fromValue(status);
                order.setStatus(newStatus);
                order.setUpdatedAt(LocalDateTime.now());
                Map<String, Object> payload = new HashMap<>();
                payload.put("orderId", order.getId());
                payload.put("status", newStatus.name());
                payload.put("displayName", newStatus.getDisplayName());
                ObjectMapper mapper = new ObjectMapper();
                 String jsonPayload = mapper.writeValueAsString(payload);
                messagingTemplate.convertAndSend("/topic/delivery/" + order.getId(), payload); // Gửi object, KHÔNG dùng ObjectMapper để stringify
                return orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("❌ Trạng thái không hợp lệ: " + status);
            } catch (Exception ex) {
                throw new RuntimeException("❌ Lỗi khi gửi WebSocket JSON", ex);
            }
        }
        return null;
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
