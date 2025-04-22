package com.example.duangiatsay.controller;

import com.example.duangiatsay.dto.ChangePasswordRequest;
import com.example.duangiatsay.dto.ForGotPassWordDTO;
import com.example.duangiatsay.dto.ResetPasswordDTO;
import com.example.duangiatsay.dto.VerifyOtpDTO;
import com.example.duangiatsay.model.Account;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.service.IAccountService;
import com.example.duangiatsay.service.IUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AccountRestController {

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IUserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private String createJwtToken(String username, String role) {
        long expirationTime = 1000 * 60 * 60 * 24; // 1 day
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            Map<String, Object> loginResult = accountService.validateLogin(username, password);
            boolean success = (boolean) loginResult.get("success");

            if (success) {
                String role = accountService.getRoleIdByUsername(username);
                String token = createJwtToken(username, role);
                User user = userService.getUserByUsername(username);

                // Gửi thông báo WebSocket khi đăng nhập thành công
                messagingTemplate.convertAndSend("/topic/notifications",
                        "🔔 Người dùng '" + username + "' đã đăng nhập thành công!");

                response.put("success", true);
                response.put("message", "Đăng nhập thành công");
                response.put("token", token);
                response.put("username", username);
                response.put("role", role != null ? role.toLowerCase() : null);
                response.put("userId", user != null ? user.getId() : null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", loginResult.get("message"));
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xử lý yêu cầu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/lock/{id}")
    public ResponseEntity<Map<String, Object>> lockAccount(@PathVariable Long id) {
        Map<String, Object> response = accountService.lockAccount(id);
        boolean success = (boolean) response.get("success");
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập!");
        }

        String username = principal.getName();
        try {
            String oldPassword = changePasswordRequest.getOldPassword();
            String newPassword = changePasswordRequest.getNewPassword();

            if (newPassword == null || newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("❌ Mật khẩu mới phải có ít nhất 8 ký tự.");
            }

            boolean isChanged = accountService.changePassword(username, oldPassword, newPassword, null);

            if (isChanged) {
                return ResponseEntity.ok("✅ Mật khẩu đã được thay đổi thành công!");
            } else {
                return ResponseEntity.badRequest().body("❌ Mật khẩu cũ không đúng hoặc có lỗi xảy ra.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Đã xảy ra lỗi trong quá trình thay đổi mật khẩu.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForGotPassWordDTO request) {
        Map<String, Object> response = accountService.forgotPassword(request.getEmailOrUsername());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpDTO request) {
        Map<String, Object> response = accountService.verifyOtp(request.getEmailOrUsername(), request.getOtp());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        if (request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        Map<String, Object> response = accountService.newPassword(request.getEmailOrUsername(), request.getNewPassword());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/role")
    public ResponseEntity<String> getRole(@RequestParam String username) {
        return ResponseEntity.ok(accountService.getRoleIdByUsername(username));
    }
    // AccountController.java
    @GetMapping("/shippers")
    public ResponseEntity<List<Account>> getAllShippers() {
        return ResponseEntity.ok(accountService.getAllShippers());
    }

}
