package com.example.duangiatsay.repository;

import com.example.duangiatsay.model.LaundryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaundryItemRepository extends JpaRepository<LaundryItem, Long> {
    List<LaundryItem> findByOrderId(Long orderId);
}