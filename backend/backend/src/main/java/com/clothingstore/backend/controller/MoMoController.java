package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.MoMoCreateRequest;
import com.clothingstore.backend.dto.request.MoMoIPNRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.MoMoCreateResponse;
import com.clothingstore.backend.service.MoMoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý Thanh Toán Ví Điện Tử MoMo (MoMo Sandbox / Test Gateway).
 * Base path: /api/v1/payments/momo
 */
@RestController
@RequestMapping("/api/v1/payments/momo")
@RequiredArgsConstructor
public class MoMoController {

    private final MoMoService moMoService;

    /**
     * POST /api/v1/payments/momo/create
     * Khởi tạo giao dịch MoMo Sandbox và nhận payUrl.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MoMoCreateResponse>> createPayment(
            @Valid @RequestBody MoMoCreateRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(moMoService.createPayment(email, request));
    }

    /**
     * POST /api/v1/payments/momo/ipn
     * Webhook IPN Callback từ MoMo (Chống trùng lập Idempotent, verify signature).
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleIPN(@RequestBody MoMoIPNRequest ipnRequest) {
        return ResponseEntity.ok(moMoService.handleIPN(ipnRequest));
    }

    /**
     * GET /api/v1/payments/momo/status/{orderId}
     * Query kiểm tra trạng thái thanh toán đơn hàng MoMo.
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(moMoService.getPaymentStatusByOrder(orderId));
    }

    /**
     * POST /api/v1/payments/momo/confirm-mock
     * API xác nhận thanh toán MoMo Sandbox ảo cho môi trường kiểm thử.
     */
    @PostMapping("/confirm-mock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmMockPayment(
            @RequestParam Long orderId,
            @RequestParam String requestId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(moMoService.confirmMockPayment(orderId, requestId, status));
    }
}
