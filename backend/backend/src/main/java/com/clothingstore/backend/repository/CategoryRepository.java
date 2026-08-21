package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng `categories`.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    Optional<Category> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, Long id);

    /**
     * Lấy toàn bộ danh sách chưa xóa mềm (dùng cho dropdown cha).
     */
    List<Category> findAllByDeletedAtIsNullOrderBySortOrderAscNameAsc();

    /**
     * Lấy toàn bộ danh sách active phía Client.
     */
    List<Category> findAllByStatusAndDeletedAtIsNullOrderBySortOrderAscNameAsc(String status);
}
