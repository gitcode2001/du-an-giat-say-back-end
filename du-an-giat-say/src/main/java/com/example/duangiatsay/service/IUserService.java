package com.example.duangiatsay.service;

import com.example.duangiatsay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService extends IService<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username); // Tên hàm hợp lý
    User getUserByUsername(String username);
    Page<User> findAllUser(Pageable pageable, String search);
}
