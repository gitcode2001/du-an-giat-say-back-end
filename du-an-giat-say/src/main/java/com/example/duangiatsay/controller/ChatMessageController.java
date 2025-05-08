package com.example.duangiatsay.controller;

import com.example.duangiatsay.dto.ChatMessageDTO;
import com.example.duangiatsay.model.ChatMessage;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.service.implement.ChatService;
import com.example.duangiatsay.config.WebSocketSessionTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatMessageController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketSessionTracker sessionTracker;

    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatMessageDTO dto, Principal principal) {
        String senderUsername = principal.getName();

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        // Kiểm tra vai trò
        if (receiver.getAccount() == null || !"STAFF".equalsIgnoreCase(String.valueOf(receiver.getAccount().getRole()))) {
            throw new IllegalArgumentException("Chỉ có thể gửi tin nhắn cho nhân viên.");
        }

        // Lưu tin nhắn
        ChatMessage chatMessage = new ChatMessage(sender, receiver, dto.getContent(), false);
        chatService.saveMessage(chatMessage);

        boolean isReceiverOnline = sessionTracker.isUserOnline(receiver.getAccount().getUsername());

        if (isReceiverOnline) {
            messagingTemplate.convertAndSendToUser(receiver.getAccount().getUsername(), "/queue/messages", dto.getContent());
        } else {
            String aiResponse = "AI Response: " + dto.getContent();
            ChatMessage aiMessage = new ChatMessage(null, sender, aiResponse, true);
            chatService.saveMessage(aiMessage);
            messagingTemplate.convertAndSendToUser(sender.getAccount().getUsername(), "/queue/messages", aiResponse);
        }
    }

}
