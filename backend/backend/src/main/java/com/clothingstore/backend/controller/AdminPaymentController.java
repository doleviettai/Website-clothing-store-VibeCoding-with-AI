package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.PaymentResponse;
import com.clothingstore.backend.service.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Quản Lý Giao Dịch Thanh Toán Dành Cho Admin.
 * Base path: /api/v1/admin/payments
 */
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    /**
     * GET /api/v1/admin/payments
     * Danh sách lịch sử giao dịch phân trang, tìm kiếm & lọc cổng thanh toán, trạng thái.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String paymentGateway,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminPaymentService.getPayments(keyword, paymentGateway, status, page, size));
    }

    /**
     * GET /api/v1/admin/payments/{id}
     * Chi tiết lịch sử giao dịch.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminPaymentService.getPaymentDetail(id));
    }
}
