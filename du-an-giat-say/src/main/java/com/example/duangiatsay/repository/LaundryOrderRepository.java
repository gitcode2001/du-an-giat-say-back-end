package com.example.duangiatsay.repository;

import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, Long> {
    List<LaundryOrder> findByUserId(Long userId);
    List<LaundryOrder> findByStatus(OrderStatus status);
}