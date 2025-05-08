package com.example.duangiatsay.controller;

import com.example.duangiatsay.request.VnpayRequest;
import com.example.duangiatsay.service.implement.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin(origins = "*")
public class VnpayRestController {

    @Autowired
    private VnpayService vnpayService;

    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@RequestBody VnpayRequest paymentRequest, HttpServletRequest request) {
        try {
            if (paymentRequest.getAmount() == null || !isValidAmount(paymentRequest.getAmount())) {
                return ResponseEntity.badRequest().body("Số tiền thanh toán không hợp lệ");
            }
            String clientIp = getClientIp(request);
            paymentRequest.setIpAddress(clientIp);
            String paymentUrl = vnpayService.createPayment(paymentRequest);
            return ResponseEntity.ok(paymentUrl);
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.badRequest().body("Lỗi encode URL: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/payment-return")
    public ResponseEntity<String> handlePaymentReturn(@RequestParam("vnp_ResponseCode") String responseCode) {
        return vnpayService.handlePaymentReturn(responseCode);
    }
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private boolean isValidAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
