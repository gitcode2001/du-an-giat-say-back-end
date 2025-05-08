package com.example.duangiatsay.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessageDTO {
    // Getters and Setters
    private Long senderId;
    private Long receiverId;
    private String content;

}
