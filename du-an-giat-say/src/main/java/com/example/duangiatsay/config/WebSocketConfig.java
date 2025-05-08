package com.example.duangiatsay.config;

import com.example.duangiatsay.component.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ Broker để gửi tin nhắn về client
        config.enableSimpleBroker("/topic", "/user"); // Cho phép gửi tin riêng
        config.setApplicationDestinationPrefixes("/app"); // Tin nhắn client gửi tới
        config.setUserDestinationPrefix("/user"); // Tin nhắn gửi riêng tới người dùng
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Một endpoint duy nhất cho client WebSocket kết nối
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Hoặc cụ thể domain frontend
                .withSockJS();
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtHandshakeInterceptor);
    }
}
