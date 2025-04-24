package com.example.duangiatsay.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.example.duangiatsay.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Autowired
    private IPayService payService;

    private static final String SUCCESS_URL = "http://localhost:8080/api/payment/success";
    private static final String CANCEL_URL = "http://localhost:8080/api/payment/cancel";

    @PostMapping("/create")
    public Map<String, String> createPayment(@RequestParam("amount") Double amount,
                                             @RequestParam(value = "orderId", required = false) Long orderId) {
        Map<String, String> response = new HashMap<>();
        try {
            Payment payment = payService.createPaymentWithPayPal(
                    amount,
                    "USD",
                    "paypal",
                    "sale",
                    "Thanh toán đơn hàng",
                    CANCEL_URL,
                    SUCCESS_URL
            );
            for (Links link : payment.getLinks()) {
                if ("approval_url".equalsIgnoreCase(link.getRel())) {
                    response.put("redirectUrl", link.getHref());
                    return response;
                }
            }
            response.put("error", "Không tìm thấy liên kết xác nhận thanh toán.");
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            response.put("error", "Không thể tạo thanh toán");
        }
        return response;
    }

    @GetMapping("/success")
    public Map<String, String> success(@RequestParam("paymentId") String paymentId,
                                       @RequestParam("PayerID") String payerId,
                                       @RequestParam(value = "orderId", required = false) Long orderId) {
        Map<String, String> result = new HashMap<>();
        try {
            Payment payment = payService.executePayment(paymentId, payerId);
            result.put("status", "success");
            result.put("paymentId", payment.getId());
            result.put("message", "Thanh toán thành công.");
            return result;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            result.put("status", "fail");
            result.put("message", "Lỗi xác nhận thanh toán");
            return result;
        }
    }

    @GetMapping("/cancel")
    public Map<String, String> cancel(@RequestParam(value = "orderId", required = false) Long orderId) {
        Map<String, String> cancelResult = new HashMap<>();
        cancelResult.put("status", "cancelled");
        cancelResult.put("message", "Thanh toán đã bị huỷ");
        return cancelResult;
    }
}
