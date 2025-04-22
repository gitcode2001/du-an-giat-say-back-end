package com.example.duangiatsay.service;

import com.example.duangiatsay.model.LaundryItem;
import java.util.List;

public interface ILaundryItemService {
    LaundryItem save(LaundryItem item);
    List<LaundryItem> getAllByOrderId(Long orderId);
    LaundryItem getById(Long id);
    void delete(Long id);
    LaundryItem update(Long id, LaundryItem updatedItem);
}