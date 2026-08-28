package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.request.ReviewRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductReviewResponse;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.entity.ProductReview;
import com.clothingstore.backend.entity.User;
import com.clothingstore.backend.exception.AppException;
import com.clothingstore.backend.repository.ProductRepository;
import com.clothingstore.backend.repository.ProductReviewRepository;
import com.clothingstore.backend.repository.UserRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cài đặt ProductReviewService.
 */
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProductReviewResponse>> getProductReviews(Long productId) {
        List<ProductReview> reviews = reviewRepository.findAllByProductIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(productId, "VISIBLE");
        List<ProductReviewResponse> responses = reviews.stream().map(this::toReviewResponse).collect(Collectors.toList());
        return ApiResponse.success("Lấy danh sách đánh giá sản phẩm thành công", responses);
    }

    @Override
    @Transactional
    public ApiResponse<ProductReviewResponse> createReview(String userEmail, Long productId, ReviewRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để gửi đánh giá"));

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + productId));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .content(request.getComment().trim())
                .status("VISIBLE")
                .isVerifiedPurchase(true)
                .build();

        reviewRepository.save(review);

        // Tự động tính toán lại averageRating & reviewCount
        updateProductRatingSummary(product);

        return ApiResponse.success("Cảm ơn bạn đã gửi đánh giá sản phẩm!", toReviewResponse(review));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUserReview(String userEmail, Long reviewId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập"));

        ProductReview review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá với ID: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa bình luận của người khác");
        }

        review.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // Cập nhật lại rating summary
        updateProductRatingSummary(review.getProduct());

        return ApiResponse.success("Đã xóa đánh giá của bạn", null);
    }

    @Override
    @Transactional(readOnly = true)
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

        // Cập nhật lại summary rating
        updateProductRatingSummary(review.getProduct());

        String msg = "VISIBLE".equals(newStatus) ? "Đã hiển thị bình luận đánh giá" : "Đã ẩn bình luận đánh giá thành công";
        return ApiResponse.success(msg, toReviewResponse(review));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteReview(Long id) {
        ProductReview review = reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá với ID: " + id));

        review.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(review);

        updateProductRatingSummary(review.getProduct());

        return ApiResponse.success("Đã xóa bình luận đánh giá thành công", null);
    }

    private void updateProductRatingSummary(Product product) {
        if (product == null) return;
        List<ProductReview> activeReviews = reviewRepository.findAllByProductIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(product.getId(), "VISIBLE");
        int count = activeReviews.size();
        product.setReviewCount(count);
        if (count == 0) {
            product.setAverageRating(new BigDecimal("5.00"));
        } else {
            double sum = activeReviews.stream().mapToInt(ProductReview::getRating).sum();
            double avg = sum / count;
            product.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }
        productRepository.save(product);
    }

    private ProductReviewResponse toReviewResponse(ProductReview r) {
        return ProductReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                .productName(r.getProduct() != null ? r.getProduct().getName() : null)
                .productThumbnailUrl(r.getProduct() != null ? r.getProduct().getThumbnailUrl() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userFullName(r.getUser() != null ? (r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getEmail()) : "Khách hàng")
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
