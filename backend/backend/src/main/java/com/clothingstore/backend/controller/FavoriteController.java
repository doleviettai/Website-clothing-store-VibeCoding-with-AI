package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.FavoriteRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.FavoriteResponse;
import com.clothingstore.backend.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý Sản phẩm yêu thích cá nhân của khách hàng.
 * Base path: /api/v1/favorites
 */
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * POST /api/v1/favorites
     * Thêm sản phẩm vào danh sách yêu thích.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @Valid @RequestBody FavoriteRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(favoriteService.addFavorite(email, request.getProductId()));
    }

    /**
     * DELETE /api/v1/favorites/{productId}
     * Xóa sản phẩm khỏi danh sách yêu thích.
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(favoriteService.removeFavorite(email, productId));
    }

    /**
     * GET /api/v1/favorites/check/{productId}
     * Kiểm tra sản phẩm đã nằm trong danh sách yêu thích chưa.
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(favoriteService.checkFavorite(email, productId));
    }

    /**
     * GET /api/v1/favorites
     * Lấy toàn bộ danh sách sản phẩm yêu thích của khách hàng đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getUserFavorites(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(favoriteService.getUserFavorites(email));
    }
}
