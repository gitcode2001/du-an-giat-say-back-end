package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.repository.LaundryOrderRepository;
import com.example.duangiatsay.service.ILaundryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LaundryOrderService implements ILaundryOrderService {

    @Autowired
    private LaundryOrderRepository orderRepository;

    @Override
    public LaundryOrder createOrder(LaundryOrder order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Đảm bảo thông tin item được liên kết với order hiện tại
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
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
    public LaundryOrder updateOrderStatus(Long id, String status) {
        LaundryOrder order = getOrderById(id);
        if (order != null) {
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
