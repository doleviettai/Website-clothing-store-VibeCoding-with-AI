package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.CheckoutRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderResponse;
import com.clothingstore.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý Đặt hàng (Checkout) & Lịch sử đơn hàng phía Khách hàng.
 * Base path: /api/v1/orders
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders
     * Thực hiện lên đơn hàng (Checkout) từ giỏ hàng hiện tại.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(orderService.checkout(email, request));
    }

    /**
     * GET /api/v1/orders
     * Lấy danh sách lịch sử đơn hàng cá nhân của khách hàng đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    /**
     * GET /api/v1/orders/{id}
     * Lấy chi tiết một đơn hàng của khách hàng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getUserOrderDetail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(orderService.getUserOrderDetail(email, id));
    }

    /**
     * PATCH /api/v1/orders/{id}/cancel
     * Khách hàng hủy đơn hàng (chỉ khi đơn ở trạng thái PENDING).
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelUserOrder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(orderService.cancelUserOrder(email, id));
    }
}
