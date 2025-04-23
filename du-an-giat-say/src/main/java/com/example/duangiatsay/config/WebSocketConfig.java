package com.example.duangiatsay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // nơi client sẽ subscribe
        config.setApplicationDestinationPrefixes("/app"); // prefix cho client gửi message
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint chính
                .setAllowedOriginPatterns("*") // cho phép mọi origin (hoặc chỉnh theo domain)
                .withSockJS(); // fallback nếu không hỗ trợ WebSocket gốc
    }
}
