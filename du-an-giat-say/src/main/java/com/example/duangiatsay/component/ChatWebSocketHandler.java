package com.example.duangiatsay.component;

import com.example.duangiatsay.model.ChatMessage;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.ChatMessageRepository;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.config.WebSocketSessionTracker;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> sessions = new HashMap<>();
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final WebSocketSessionTracker sessionTracker;

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    public ChatWebSocketHandler(ChatMessageRepository chatMessageRepository,
                                UserRepository userRepository,
                                WebSocketSessionTracker sessionTracker) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.sessionTracker = sessionTracker;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            Map<String, String> queryParams = Arrays.stream(uri.getQuery().split("&"))
                    .map(s -> s.split("="))
                    .filter(pair -> pair.length == 2)
                    .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

            String userIdStr = queryParams.get("userId");
            String token = queryParams.get("token");

            if (userIdStr != null && token != null) {
                Long userId = Long.parseLong(userIdStr);

                if (jwtTokenIsValid(token, userId)) {
                    sessions.put(userId, session);

                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null && user.getAccount() != null) {
                        sessionTracker.userConnected(user.getAccount().getUsername());
                        System.out.println("WebSocket connected: " + user.getAccount().getUsername());
                    }
                } else {
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid JWT token"));
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String[] messageParts = message.getPayload().split(":");
        if (messageParts.length < 3) {
            session.sendMessage(new TextMessage("Invalid message format. Use senderId:receiverId:messageContent"));
            return;
        }

        Long senderId = Long.parseLong(messageParts[0]);
        Long receiverId = Long.parseLong(messageParts[1]);
        String content = messageParts[2];

        User sender = userRepository.findById(senderId).orElse(null);
        User receiver = userRepository.findById(receiverId).orElse(null);

        if (sender != null && receiver != null) {
            ChatMessage chatMessage = new ChatMessage(sender, receiver, content, false);
            chatMessageRepository.save(chatMessage);

            boolean isReceiverOnline = receiver.getAccount() != null &&
                    sessionTracker.isUserOnline(receiver.getAccount().getUsername());

            WebSocketSession receiverSession = sessions.get(receiverId);

            if (isReceiverOnline && receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(content));
            }

            if (!isReceiverOnline) {
                String aiResponse = "AI Response: " + content;
                ChatMessage aiMessage = new ChatMessage(null, sender, aiResponse, true);
                chatMessageRepository.save(aiMessage);

                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(aiResponse));
                }
            }
        } else {
            session.sendMessage(new TextMessage("Người gửi hoặc người nhận không tồn tại."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    private boolean jwtTokenIsValid(String token, Long expectedUserId) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();

            User user = userRepository.findByAccount_Username(username);
            return user != null && user.getId().equals(expectedUserId);
        } catch (JwtException e) {
            System.out.println("JWT token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
            return false;
        }
    }
}
