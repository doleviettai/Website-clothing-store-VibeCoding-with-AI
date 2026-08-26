package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;

/**
 * Service quản lý Đánh Giá & Bình Luận Sản Phẩm.
 */
public interface ProductReviewService {

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
