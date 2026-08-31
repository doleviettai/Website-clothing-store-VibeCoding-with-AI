package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.ZaloPayCallbackRequest;
import com.clothingstore.backend.dto.request.ZaloPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.ZaloPayCreateResponse;
import com.clothingstore.backend.service.ZaloPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý Thanh Toán Ví ZaloPay (ZaloPay Sandbox & Virtual Gateway).
 * Base path: /api/v1/payments/zalopay
 */
@RestController
@RequestMapping("/api/v1/payments/zalopay")
@RequiredArgsConstructor
public class ZaloPayController {

    private final ZaloPayService zaloPayService;

    /**
     * POST /api/v1/payments/zalopay/create
     * Khởi tạo giao dịch ZaloPay và lấy order_url redirect.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ZaloPayCreateResponse>> createPayment(
            @Valid @RequestBody ZaloPayCreateRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(zaloPayService.createPayment(email, request));
    }

    /**
     * POST /api/v1/payments/zalopay/callback
     * Webhook Callback từ ZaloPay hệ thống (Idempotent, xác thực bằng KEY2).
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody ZaloPayCallbackRequest callbackRequest) {
        return ResponseEntity.ok(zaloPayService.handleCallback(callbackRequest));
    }

    /**
     * GET /api/v1/payments/zalopay/status/{orderId}
     * Query kiểm tra trạng thái thanh toán đơn hàng.
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(zaloPayService.getPaymentStatusByOrder(orderId));
    }

    /**
     * POST /api/v1/payments/zalopay/confirm-mock
     * API xác nhận thanh toán ảo ZaloPay Sandbox Demo cho môi trường kiểm thử.
     */
    @PostMapping("/confirm-mock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmMockPayment(
            @RequestParam Long orderId,
            @RequestParam String appTransId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(zaloPayService.confirmMockPayment(orderId, appTransId, status));
    }
}
