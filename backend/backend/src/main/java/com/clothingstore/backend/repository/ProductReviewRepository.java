package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng `product_reviews`.
 */
@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long>, JpaSpecificationExecutor<ProductReview> {

    Optional<ProductReview> findByIdAndDeletedAtIsNull(Long id);

    List<ProductReview> findAllByProductIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(Long productId, String status);
}
