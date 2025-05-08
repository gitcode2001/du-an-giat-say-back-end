package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.Account;
import com.example.duangiatsay.model.User;
import com.example.duangiatsay.repository.AccountRepository;
import com.example.duangiatsay.repository.RoleRepository;
import com.example.duangiatsay.repository.UserRepository;
import com.example.duangiatsay.service.IUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(User entity) {
        if (entity.getAccount() == null) {
            throw new IllegalArgumentException("Thông tin tài khoản không hợp lệ");
        }

        Account account = entity.getAccount();

        // Kiểm tra các trường email và tên đăng nhập
        if (entity.getEmail() == null || entity.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (existsByEmail(entity.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống, vui lòng sử dụng email khác");
        }

        if (account.getUsername() == null || account.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (existsByUsername(account.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác");
        }

        // Kiểm tra các trường người dùng
        if (entity.getFullName() == null || entity.getFullName().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }
        if (entity.getPhoneNumber() != null && entity.getPhoneNumber().length() != 10) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số");
        }
        if (entity.getBirthDate() == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }

        // Kiểm tra mật khẩu
        String rawPassword = account.getPassword();
        if (rawPassword.isBlank()) {
            rawPassword = generateAndStorePassword(account.getUsername());
        }
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setDateCreatePassWord(LocalDateTime.now());

        // Kiểm tra vai trò
        if (account.getRole() == null || account.getRole().getId() == null) {
            throw new IllegalArgumentException("Vai trò không tồn tại hoặc không hợp lệ");
        }
        account.setRole(roleRepository.findById(account.getRole().getId()).orElseThrow(() ->
                new IllegalArgumentException("Vai trò không hợp lệ hoặc không tìm thấy")));

        // Lưu thông tin tài khoản và người dùng
        Account savedAccount = accountRepository.save(account);
        entity.setAccount(savedAccount);
        User savedUser = userRepository.save(entity);

        // Gửi email chứa mật khẩu gốc (chưa mã hóa) cho người dùng
        emailService.sendPasswordEmail(
                savedUser.getFullName(),
                savedUser.getEmail(),
                rawPassword, // Gửi mật khẩu raw (chưa mã hóa) cho người dùng
                savedUser.getAccount().getUsername(),
                savedUser.getId()
        );

        System.out.println("✅ Đã lưu người dùng với vai trò: " + savedUser.getAccount().getRole().getRoleName());
        return savedUser;
    }

    @Override
    public void update(Long id, User entity) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            // Cập nhật các trường thông tin cá nhân
            if (entity.getFullName() != null && !entity.getFullName().isEmpty()) existingUser.setFullName(entity.getFullName());
            if (entity.getEmail() != null && !entity.getEmail().isEmpty()) existingUser.setEmail(entity.getEmail());
            if (entity.getPhoneNumber() != null) existingUser.setPhoneNumber(entity.getPhoneNumber());
            if (entity.getGender() != null) existingUser.setGender(entity.getGender());
            if (entity.getAddress() != null) existingUser.setAddress(entity.getAddress());
            if (entity.getAvatar() != null) existingUser.setAvatar(entity.getAvatar());
            if (entity.getBirthDate() != null) existingUser.setBirthDate(entity.getBirthDate());

            // Cập nhật tài khoản
            if (entity.getAccount() != null) {
                Account existingAccount = existingUser.getAccount();
                if (existingAccount == null) {
                    existingAccount = new Account();
                    existingUser.setAccount(existingAccount);
                }

                // Cập nhật tên đăng nhập và mật khẩu
                if (entity.getAccount().getUsername() != null && !entity.getAccount().getUsername().isEmpty()) {
                    existingAccount.setUsername(entity.getAccount().getUsername());
                }
                if (entity.getAccount().getPassword() != null && !entity.getAccount().getPassword().isEmpty()) {
                    existingAccount.setPassword(passwordEncoder.encode(entity.getAccount().getPassword()));
                }
                if (entity.getAccount().getRole() != null && entity.getAccount().getRole().getId() != null) {
                    existingAccount.setRole(roleRepository.findById(entity.getAccount().getRole().getId()).orElse(null));
                }

                // Cập nhật trạng thái khóa tài khoản
                existingAccount.setLocked(entity.getAccount().getLocked());
                existingAccount.setDateCreatePassWord(LocalDateTime.now());
            }

            userRepository.save(existingUser);
        }
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByAccount_Username(username);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByAccount_Username(username);
    }

    @Override
    public Page<User> findAllUser(Pageable pageable, String search) {
        return userRepository.findAllUsers(
                search,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending())
        );
    }

    private String generateAndStorePassword(String username) {
        // Tạo mật khẩu với độ dài 12 ký tự và đảm bảo mật khẩu mạnh hơn
        String rawPassword = RandomStringUtils.randomAlphanumeric(12); // Tăng độ dài mật khẩu
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpSession session = attributes.getRequest().getSession();
            session.setAttribute("rawPassword_" + username, rawPassword);
        }
        return rawPassword;
    }
}
