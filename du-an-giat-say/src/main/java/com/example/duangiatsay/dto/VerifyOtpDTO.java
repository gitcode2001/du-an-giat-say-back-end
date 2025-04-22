package com.example.duangiatsay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpDTO {
    private String emailOrUsername;
    private String otp;
}
