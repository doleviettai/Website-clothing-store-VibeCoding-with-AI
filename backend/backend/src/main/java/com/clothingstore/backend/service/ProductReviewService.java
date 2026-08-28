package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.ReviewRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;

import java.util.List;

/**
 * Service quản lý Đánh Giá & Bình Luận Sản Phẩm.
 */
public interface ProductReviewService {

    /**
     * Client: Lấy danh sách bình luận đánh giá công khai (VISIBLE) của một sản phẩm.
     */
    ApiResponse<List<ProductReviewResponse>> getProductReviews(Long productId);

    /**
     * Client: Đăng bình luận đánh giá sản phẩm mới (1-5 sao).
     */
    ApiResponse<ProductReviewResponse> createReview(String userEmail, Long productId, ReviewRequest request);

    /**
     * Client: Xóa đánh giá thuộc về chính mình.
     */
    ApiResponse<Void> deleteUserReview(String userEmail, Long reviewId);

    /**
     * Admin: Lấy danh sách bình luận đánh giá phân trang, tìm kiếm & lọc trạng thái (VISIBLE/HIDDEN).
     */
    ApiResponse<PageResponse<ProductReviewResponse>> getAdminReviews(
            String keyword, String status, Integer rating, int page, int size
    );

    /**
     * Admin: Ẩn / Hiện (Toggle) bình luận đánh giá.
     */
    ApiResponse<ProductReviewResponse> toggleReviewStatus(Long id, String status);

    /**
     * Admin: Xóa bình luận đánh giá.
     */
    ApiResponse<Void> deleteReview(Long id);
}
