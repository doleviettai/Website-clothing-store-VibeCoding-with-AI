package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.entity.ProductReview;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.ProductReviewRepository;
import com.clothingstore.backend.service.ProductReviewService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt ProductReviewService.
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;

    @Override
    public ApiResponse<PageResponse<ProductReviewResponse>> getAdminReviews(
            String keyword, String status, Integer rating, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<ProductReview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy bình luận chưa bị xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";

                Join<ProductReview, Product> productJoin = root.join("product");
                Join<ProductReview, User> userJoin = root.join("user");

                predicates.add(cb.or(
                        cb.like(cb.lower(productJoin.get("name")), likeKeyword),
                        cb.like(cb.lower(userJoin.get("fullName")), likeKeyword),
                        cb.like(cb.lower(userJoin.get("email")), likeKeyword),
                        cb.like(cb.lower(root.get("content")), likeKeyword)
                ));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductReview> reviewPage = reviewRepository.findAll(spec, pageable);
        Page<ProductReviewResponse> responsePage = reviewPage.map(this::toReviewResponse);

        return ApiResponse.success("Lấy danh sách đánh giá bình luận thành công", PageResponse.from(responsePage));
    }

    @Override
    @Transactional
    public ApiResponse<ProductReviewResponse> toggleReviewStatus(Long id, String status) {
        ProductReview review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá với ID: " + id));

        String newStatus = (status != null && !status.isBlank()) ? status : ("VISIBLE".equals(review.getStatus()) ? "HIDDEN" : "VISIBLE");
        review.setStatus(newStatus);
        reviewRepository.save(review);

        String msg = "VISIBLE".equals(newStatus) ? "Đã hiển thị bình luận đánh giá" : "Đã ẩn bình luận đánh giá thành công";
        return ApiResponse.success(msg, toReviewResponse(review));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteReview(Long id) {
        ProductReview review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá với ID: " + id));

        review.setDeletedAt(LocalDateTime.now());
        review.setStatus("HIDDEN");
        reviewRepository.save(review);

        return ApiResponse.success("Đã xóa bình luận đánh giá thành công");
    }

    // ================= Private helpers =================

    private ProductReviewResponse toReviewResponse(ProductReview r) {
        return ProductReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                .productName(r.getProduct() != null ? r.getProduct().getName() : null)
                .productThumbnailUrl(r.getProduct() != null ? r.getProduct().getThumbnailUrl() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userFullName(r.getUser() != null ? r.getUser().getFullName() : null)
                .userEmail(r.getUser() != null ? r.getUser().getEmail() : null)
                .userAvatarUrl(r.getUser() != null ? r.getUser().getAvatarUrl() : null)
                .rating(r.getRating())
                .content(r.getContent())
                .status(r.getStatus())
                .isVerifiedPurchase(r.getIsVerifiedPurchase())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
