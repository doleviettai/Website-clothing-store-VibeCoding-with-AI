package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.CartItemRequest;
import com.clothingstore.backend.dto.request.CartItemUpdateCountRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CartResponse;
import com.clothingstore.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý Giỏ hàng của khách hàng.
 * Base path: /api/v1/cart
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/v1/cart
     * Lấy thông tin giỏ hàng cá nhân của khách hàng đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cartService.getCart(email));
    }

    /**
     * POST /api/v1/cart/items
     * Thêm sản phẩm vào giỏ hàng (productId, quantity, size, color).
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cartService.addToCart(email, request));
    }

    /**
     * PUT /api/v1/cart/items/{itemId}
     * Cập nhật số lượng của một dòng sản phẩm trong giỏ hàng.
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateCountRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cartService.updateCartItemQuantity(email, itemId, request));
    }

    /**
     * DELETE /api/v1/cart/items/{itemId}
     * Xóa một sản phẩm khỏi giỏ hàng.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cartService.removeCartItem(email, itemId));
    }
}
