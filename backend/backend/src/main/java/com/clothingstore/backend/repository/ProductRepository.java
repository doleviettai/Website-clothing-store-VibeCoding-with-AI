package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng `products`.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Optional<Product> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);

    List<Product> findAllByStatusAndDeletedAtIsNullOrderByIdDesc(String status);
}
