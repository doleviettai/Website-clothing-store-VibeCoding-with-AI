package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng `banners`.
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long>, JpaSpecificationExecutor<Banner> {

    Optional<Banner> findByIdAndDeletedAtIsNull(Long id);

    Optional<Banner> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);

    /**
     * Lấy danh sách banner ACTIVE theo vị trí (position) được sắp xếp theo thứ tự sortOrder tăng dần.
     */
    List<Banner> findAllByPositionAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(String position, String status);

    List<Banner> findAllByStatusAndDeletedAtIsNullOrderBySortOrderAsc(String status);
}
