package com.example.duangiatsay.service;

import com.example.duangiatsay.model.LaundryOrder;

import java.util.List;

public interface ILaundryOrderService {
    LaundryOrder createOrder(LaundryOrder order);
    LaundryOrder getOrderById(Long id);
    List<LaundryOrder> getAllOrders();
    List<LaundryOrder> getOrdersByUserId(Long userId);
    void softDeleteOrderByShipper(Long id);
    LaundryOrder updateOrderStatus(Long id, String status);
    void deleteOrder(Long id);
    List<LaundryOrder> getOrdersByShipperId(Long shipperId);

    void softDeleteOrderByAdmin(Long id);

    List<LaundryOrder> getOrdersByShipperIdAndStatus(Long shipperId, String status);
}
