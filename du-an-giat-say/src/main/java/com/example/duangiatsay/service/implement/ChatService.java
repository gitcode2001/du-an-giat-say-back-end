package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.ChatMessage;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

    @Service
    public class ChatService {

        @Autowired
        private ChatMessageRepository chatMessageRepository;

        public ChatMessage saveMessage(ChatMessage message) {
            return chatMessageRepository.save(message);
        }

        public List<ChatMessage> getChatHistory(User sender, User receiver) {
            return chatMessageRepository.findBySenderAndReceiverOrderByTimestampAsc(sender, receiver);
        }
    }
