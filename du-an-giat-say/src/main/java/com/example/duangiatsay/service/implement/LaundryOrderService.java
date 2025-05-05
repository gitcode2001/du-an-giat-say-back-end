package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.LaundryItem;
import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.model.OrderStatus;
import com.example.duangiatsay.model.PaymentMethod;
import com.example.duangiatsay.repository.LaundryOrderRepository;
import com.example.duangiatsay.service.ILaundryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (order.getPaymentMethod() == null) {
            order.setPaymentMethod(PaymentMethod.COD);
        }

        if (order.getItems() != null) {
            for (LaundryItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        LaundryOrder savedOrder = orderRepository.save(order);

        if (savedOrder.getShipper() != null && savedOrder.getShipper().getId() != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("orderId", savedOrder.getId());
            notification.put("status", savedOrder.getStatus().name());
            notification.put("message", "📦 Bạn có đơn hàng mới từ " + savedOrder.getUser().getFullName());
            messagingTemplate.convertAndSend("/topic/shipper/" + savedOrder.getShipper().getId(), notification);
        }

        return savedOrder;
    }

    @Override
    public LaundryOrder getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<LaundryOrder> getAllOrders() {
        return orderRepository.findByDeletedByAdminFalse();
    }

    @Override
    public List<LaundryOrder> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<LaundryOrder> getOrdersByShipperId(Long shipperId) {
        return orderRepository.findByShipperId(shipperId);
    }

    public List<LaundryOrder> getOrdersByShipperIdAndStatus(Long shipperId, String status) {
        try {
            OrderStatus orderStatus = OrderStatus.fromValue(status);
            return orderRepository.findByShipperId(shipperId).stream()
                    .filter(order -> !order.getDeletedByShipper() && order.getStatus() == orderStatus)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("❌ Trạng thái không hợp lệ: " + status);
        }
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
    public void softDeleteOrderByAdmin(Long id) {
        LaundryOrder order = getOrderById(id);
        if (order != null) {
            order.setDeletedByAdmin(true);
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

                messagingTemplate.convertAndSend("/topic/delivery/" + order.getId(), payload);

                if (order.getShipper() != null && order.getShipper().getId() != null) {
                    messagingTemplate.convertAndSend(
                            "/topic/shipper/" + order.getShipper().getId(),
                            Map.of(
                                    "orderId", order.getId(),
                                    "status", newStatus.name(),
                                    "message", "📦 Trạng thái đơn hàng đã được cập nhật thành: " + newStatus.getDisplayName()
                            )
                    );
                }

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
