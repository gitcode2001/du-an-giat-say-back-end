package com.example.duangiatsay.service.implement;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {
    private final Map<String, OtpEntry> otps = new HashMap<>();
    private static final long OTP_EXPIRATION_TIME = 900;
    public void saveOtp(String indentifier, String otp) {
        otps.put(indentifier, new OtpEntry(otp, Instant.now().plusSeconds(OTP_EXPIRATION_TIME)));
    }
    public String getOtp(String indentifier) {
        OtpEntry entry = otps.get(indentifier);
        return (entry != null && entry.isValid()) ? entry.otp : null;
    }
    public boolean validateOtp(String indentifier, String otp) {
        OtpEntry entry = otps.get(indentifier);
        if(entry != null && entry.isValid() && entry.otp.equals(otp)) {
            otps.remove(indentifier);
            return true;
        }
        return false;
    }
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOtps() {
        Instant now = Instant.now();
        otps.entrySet().removeIf(entry -> entry.getValue().expiry.isBefore(now));
    }

    private record OtpEntry (String otp, Instant expiry) {
        public boolean isValid() {
            return Instant.now().isBefore(expiry);
        }
    }
}
