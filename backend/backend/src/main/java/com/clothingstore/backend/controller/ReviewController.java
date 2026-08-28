package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.ReviewRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;
import com.clothingstore.backend.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý Đánh giá & Bình luận sản phẩm công khai.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ProductReviewService reviewService;

    /**
     * GET /api/v1/products/{productId}/reviews
     * Lấy danh sách đánh giá hiển thị (VISIBLE) của sản phẩm.
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<ProductReviewResponse>>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    /**
     * POST /api/v1/products/{productId}/reviews
     * Gửi bình luận đánh giá sản phẩm (1-5 sao).
     */
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(reviewService.createReview(email, productId, request));
    }

    /**
     * DELETE /api/v1/reviews/{reviewId}
     * Khách hàng xóa bình luận đánh giá của chính mình.
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserReview(
            @PathVariable Long reviewId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(reviewService.deleteUserReview(email, reviewId));
    }
}
