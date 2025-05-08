package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.Account;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.AccountRepository;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService implements IAccountService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Map<String, Object> validateLogin(String username, String password) {
        Account account = accountRepository.findByUsername(username);

        if (account == null || account.isLocked()) {
            return Map.of("success", false, "message", "Tài khoản không tồn tại hoặc đã bị khóa");
        }

        if (!passwordEncoder.matches(password, account.getPassword())) {
            return Map.of("success", false, "message", "Mật khẩu không đúng");
        }

        return Map.of("success", true, "message", "Đăng nhập thành công");
    }

    @Override
    public boolean changePassword(String userName, String oldPassword, String newPassword, String oldPasswordRaw) {
        Account account = accountRepository.findByUsername(userName);
        if (account == null) return false;

        if (oldPasswordRaw == null || !oldPasswordRaw.equals(oldPassword)) {
            if (!passwordEncoder.matches(oldPassword, account.getPassword())) return false;
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        return true;
    }

    @Override
    public String getRoleIdByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        return (account != null && account.getRole() != null) ? account.getRole().getRoleName() : null;
    }

    @Override
    public Map<String, Object> forgotPassword(String emailOrUsername) {
        boolean usernameExists = userRepository.existsByAccount_Username(emailOrUsername);
        boolean emailExists = userRepository.existsByEmail(emailOrUsername);

        if (!usernameExists && !emailExists) {
            return Map.of("success", false, "message", "Không tìm thấy tài khoản");
        }

        User user = usernameExists
                ? userRepository.findByAccount_Username(emailOrUsername)
                : userRepository.findByEmail(emailOrUsername);

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpService.saveOtp(emailOrUsername, otp);
        emailService.sendOtpEmail(user.getFullName(), user.getEmail(), otp);

        return Map.of("success", true, "message", "Mã OTP đã được gửi đến email của bạn");
    }

    @Override
    public Map<String, Object> verifyOtp(String emailOrUsername, String otp) {
        boolean valid = otpService.validateOtp(emailOrUsername, otp);
        return Map.of("success", valid, "message", valid ? "OTP hợp lệ, bạn có thể đổi mật khẩu" : "OTP không hợp lệ hoặc đã hết hạn");
    }

    @Override
    public Map<String, Object> newPassword(String emailOrUsername, String password) {
        boolean usernameExists = userRepository.existsByAccount_Username(emailOrUsername);
        boolean emailExists = userRepository.existsByEmail(emailOrUsername);

        if (!usernameExists && !emailExists) {
            return Map.of("success", false, "message", "Không tìm thấy tài khoản");
        }

        User user = usernameExists
                ? userRepository.findByAccount_Username(emailOrUsername)
                : userRepository.findByEmail(emailOrUsername);

        Account account = user.getAccount();
        account.setPassword(passwordEncoder.encode(password));
        accountRepository.save(account);

        return Map.of("success", true, "message", "Mật khẩu đã được cập nhật thành công");
    }

    @Override
    public Map<String, Object> lockAccount(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return Map.of("success", false, "message", "Người dùng không tồn tại");
        }

        User user = optionalUser.get();
        Account account = user.getAccount();
        if (account == null) {
            return Map.of("success", false, "message", "Tài khoản không tồn tại");
        }

        boolean currentlyLocked = account.isLocked();
        account.setLocked(!currentlyLocked);
        accountRepository.save(account);

        String status = currentlyLocked ? "mở khoá" : "khoá";

        messagingTemplate.convertAndSend("/topic/account-lock", Map.of(
                "userId", userId,
                "locked", !currentlyLocked,
                "message", "🔐 Tài khoản '" + user.getFullName() + "' đã được " + status
        ));

        return Map.of("success", true, "message", "Tài khoản đã được " + status);
    }

    @Override
    public Account findAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    @Override
    public List<Account> getAllShippers() {
        return accountRepository.findAllShipperAccounts();
    }
}
