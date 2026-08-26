package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;
import com.clothingstore.backend.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Admin cho Quản lý Đánh Giá & Bình Luận Sản Phẩm (Xem, Tìm kiếm AJAX, Ẩn / Hiện, Xóa).
 * Base path: /api/v1/admin/reviews
 */
@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ProductReviewService reviewService;

    /**
     * GET /api/v1/admin/reviews
     * Lấy danh sách đánh giá phân trang, tìm kiếm & lọc theo trạng thái/rating.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductReviewResponse>>> getAdminReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getAdminReviews(keyword, status, rating, page, size));
    }

    /**
     * PATCH /api/v1/admin/reviews/{id}/status
     * Ẩn hoặc Hiện bình luận đánh giá.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> toggleReviewStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(reviewService.toggleReviewStatus(id, status));
    }

    /**
     * DELETE /api/v1/admin/reviews/{id}
     * Xóa bình luận đánh giá.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.deleteReview(id));
    }
}
