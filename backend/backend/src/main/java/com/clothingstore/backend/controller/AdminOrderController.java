package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.OrderStatusUpdateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Quản Lý Đơn Hàng Dành Cho Admin.
 * Base path: /api/v1/admin/orders
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * GET /api/v1/admin/orders
     * Danh sách đơn hàng phân trang, tìm kiếm theo keyword (Mã đơn, Tên KH, SĐT, Email) & lọc trạng thái.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminOrderService.getOrders(keyword, status, paymentStatus, page, size));
    }

    /**
     * GET /api/v1/admin/orders/{id}
     * Chi tiết đơn hàng và sản phẩm trong đơn.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderDetail(id));
    }

    /**
     * PATCH /api/v1/admin/orders/{id}/status
     * Cập nhật trạng thái đơn hàng (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, request));
    }

    /**
     * PATCH /api/v1/admin/orders/{id}/payment-status
     * Cập nhật trạng thái thanh toán (UNPAID, PAID, REFUNDED).
     */
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus
    ) {
        return ResponseEntity.ok(adminOrderService.updatePaymentStatus(id, paymentStatus));
    }
}
