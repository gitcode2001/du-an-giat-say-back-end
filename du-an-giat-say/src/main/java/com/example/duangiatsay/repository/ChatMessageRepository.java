package com.example.duangiatsay.repository;

import com.example.duangiatsay.model.ChatMessage;
import com.example.duangiatsay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);
}
