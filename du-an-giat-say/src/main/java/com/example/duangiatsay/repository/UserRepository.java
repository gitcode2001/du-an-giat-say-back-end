package com.example.duangiatsay.repository;

import com.example.duangiatsay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByFullNameContaining(String keyword);
}