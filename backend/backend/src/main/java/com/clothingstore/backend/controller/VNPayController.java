package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.VNPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.VNPayCreateResponse;
import com.clothingstore.backend.service.VNPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý Thanh Toán Cổng VNPAY (VNPAY Sandbox / Virtual Gateway).
 * Base path: /api/v1/payments/vnpay
 */
@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;

    /**
     * POST /api/v1/payments/vnpay/create
     * Khởi tạo giao dịch VNPAY Sandbox và lấy paymentUrl.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VNPayCreateResponse>> createPayment(
            @Valid @RequestBody VNPayCreateRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(vnPayService.createPayment(email, request));
    }

    /**
     * GET /api/v1/payments/vnpay/status/{orderId}
     * Query kiểm tra trạng thái thanh toán đơn hàng VNPAY.
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(vnPayService.getPaymentStatusByOrder(orderId));
    }

    /**
     * POST /api/v1/payments/vnpay/confirm-mock
     * API xác nhận thanh toán VNPAY Sandbox ảo cho môi trường kiểm thử.
     */
    @PostMapping("/confirm-mock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmMockPayment(
            @RequestParam Long orderId,
            @RequestParam String txnRef,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(vnPayService.confirmMockPayment(orderId, txnRef, status));
    }
}
