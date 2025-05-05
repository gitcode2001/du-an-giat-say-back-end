package com.example.duangiatsay.controller;

import com.example.duangiatsay.model.LaundryOrder;
import com.example.duangiatsay.model.OrderStatus;
import com.example.duangiatsay.model.PaymentMethod;
import com.example.duangiatsay.service.ILaundryOrderService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.example.duangiatsay.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
// PaymentController.java
@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Autowired
    private IPayService payService;
    @Autowired
    private ILaundryOrderService laundryOrderService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String SUCCESS_URL = "http://localhost:3000/payment/success";
    private static final String CANCEL_URL = "http://localhost:8080/api/payment/cancel";

    @PostMapping("/create")
    public Map<String, String> createPayment(@RequestParam("amount") Double amount,
                                             @RequestParam(value = "orderId", required = false) Long orderId) {
        Map<String, String> response = new HashMap<>();
        try {
            Payment payment = payService.createPaymentWithPayPal(
                    amount, "USD", "paypal", "sale",
                    "Thanh toán đơn hàng", CANCEL_URL, SUCCESS_URL
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
            response.put("error", "Không thể tạo thanh toán. Vui lòng thử lại sau.");
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

            if (orderId != null) {
                LaundryOrder order = laundryOrderService.getOrderById(orderId);
                if (order != null && order.getPaymentMethod() == PaymentMethod.PAYPAL) {
                    if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PICKED_UP) {
                        result.put("status", "fail");
                        result.put("message", "PAYMENT_ALREADY_DONE");
                        return result;
                    }
                    order.setStatus(OrderStatus.PICKED_UP);
                    order.setUpdatedAt(LocalDateTime.now());
                    laundryOrderService.createOrder(order);

                    Map<String, Object> wsPayload = new HashMap<>();
                    wsPayload.put("orderId", order.getId());
                    wsPayload.put("message", "✅ Thanh toán PayPal thành công cho đơn hàng #" + order.getId());
                    messagingTemplate.convertAndSend("/topic/payment/" + order.getUser().getId(), wsPayload);
                }
            }

            result.put("status", "success");
            result.put("paymentId", payment.getId());
            result.put("message", "Thanh toán thành công.");
            return result;
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            result.put("status", "fail");
            result.put("message", "Lỗi xác nhận thanh toán. Vui lòng thử lại.");
            return result;
        }
    }

    @GetMapping("/cancel")
    public Map<String, String> cancel(@RequestParam(value = "orderId", required = false) Long orderId) {
        Map<String, String> cancelResult = new HashMap<>();
        cancelResult.put("status", "cancelled");
        cancelResult.put("message", "Thanh toán đã bị huỷ.");
        return cancelResult;
    }
}
