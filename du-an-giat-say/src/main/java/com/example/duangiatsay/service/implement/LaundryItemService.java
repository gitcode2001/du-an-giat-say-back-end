package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.LaundryItem;
import com.example.duangiatsay.repository.LaundryItemRepository;
import com.example.duangiatsay.service.ILaundryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaundryItemService implements ILaundryItemService {

    @Autowired
    private LaundryItemRepository laundryItemRepository;

    @Override
    public LaundryItem save(LaundryItem item) {
        return laundryItemRepository.save(item);
    }

    @Override
    public List<LaundryItem> getAllByOrderId(Long orderId) {
        return laundryItemRepository.findByOrderId(orderId);
    }

    @Override
    public LaundryItem getById(Long id) {
        return laundryItemRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        laundryItemRepository.deleteById(id);
    }

    @Override
    public LaundryItem update(Long id, LaundryItem updatedItem) {
        LaundryItem existing = laundryItemRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setType(updatedItem.getType());
            existing.setQuantity(updatedItem.getQuantity());
            existing.setPricePerItem(updatedItem.getPricePerItem());
            existing.setDescription(updatedItem.getDescription());
            return laundryItemRepository.save(existing);
        }
        return null;
    }
}
