package com.example.duangiatsay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    private String emailOrUsername;
    private String otp;
    private String newPassword;
}
