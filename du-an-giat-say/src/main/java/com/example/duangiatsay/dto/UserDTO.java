package com.example.duangiatsay.dto;

import com.example.duangiatsay.model.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private Boolean gender;
    private LocalDate birthDate;
    private String avatar;
    private String username;
    private String role;
    private Integer locked;

    public UserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.address = user.getAddress();
            this.gender = user.getGender();
            this.birthDate = user.getBirthDate();
            this.avatar = user.getAvatar();
            if (user.getAccount() != null) {
                this.username = user.getAccount().getUsername();
                this.locked = user.getAccount().getLocked();
                if (user.getAccount().getRole() != null) {
                    this.role = user.getAccount().getRole().getNameRoles();
                }
            }
        }
    }
}
