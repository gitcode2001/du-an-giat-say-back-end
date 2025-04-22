package com.example.duangiatsay.service;

import com.example.duangiatsay.model.Category;
import java.util.List;

public interface ICategoryService {
    List<Category> getAll();
    Category getById(Long id);
    Category save(Category category);
    Category update(Long id, Category category);
    void delete(Long id);
}