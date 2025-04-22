package com.example.duangiatsay.service;

import com.example.duangiatsay.model.User;

import java.util.List;

public interface IService<E, T> {
    List<E> getAll();
    User save(E entity);
    void update(T id, E entity);
    void delete(T id);
    E findById(T id);
}
