// Model Classes

// ... [các class model và DTO giữ nguyên như trước] ...

// Controller

package com.example.duangiatsay.controller;

import com.example.duangiatsay.model.Account;
import com.example.duangiatsay.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "*")
public class AccountRestController {

    @Autowired
    private IAccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        return ResponseEntity.ok(accountService.validateLogin(username, password));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(
            @RequestParam String username,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam(required = false) String oldPasswordRaw) {
        boolean result = accountService.changePassword(username, oldPassword, newPassword, oldPasswordRaw);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String emailOrUsername) {
        return ResponseEntity.ok(accountService.forgotPassword(emailOrUsername));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String emailOrUsername, @RequestParam String otp) {
        return ResponseEntity.ok(accountService.verifyOtp(emailOrUsername, otp));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam String emailOrUsername, @RequestParam String newPassword) {
        return ResponseEntity.ok(accountService.newPassword(emailOrUsername, newPassword));
    }

    @PostMapping("/lock/{userId}")
    public ResponseEntity<Map<String, Object>> lockAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.lockAccount(userId));
    }

    @GetMapping("/role")
    public ResponseEntity<String> getRole(@RequestParam String username) {
        return ResponseEntity.ok(accountService.getRoleIdByUsername(username));
    }
} 