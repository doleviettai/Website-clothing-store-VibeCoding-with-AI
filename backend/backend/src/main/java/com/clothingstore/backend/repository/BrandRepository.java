package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng `brands`.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {

    Optional<Brand> findByIdAndDeletedAtIsNull(Long id);

    Optional<Brand> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);

    List<Brand> findAllByDeletedAtIsNullOrderByNameAsc();

    List<Brand> findAllByStatusAndDeletedAtIsNullOrderByNameAsc(String status);
}
