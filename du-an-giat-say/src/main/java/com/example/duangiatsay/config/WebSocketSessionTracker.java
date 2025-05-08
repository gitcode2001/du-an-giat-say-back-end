package com.example.duangiatsay.config;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionTracker {
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(String username) {
        onlineUsers.add(username);
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }
}
