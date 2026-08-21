package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.CategoryRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CategoryResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service quản lý Chuyên mục.
 */
public interface CategoryService {

    /**
     * Admin: Lấy danh sách chuyên mục có phân trang, tìm kiếm & lọc.
     */
    ApiResponse<PageResponse<CategoryResponse>> getAdminCategories(
            String keyword, String status, Long parentId, int page, int size
    );

    /**
     * Lấy toàn bộ danh sách chuyên mục chưa xóa (dùng cho dropdown chuyên mục cha).
     */
    ApiResponse<List<CategoryResponse>> getAllCategoriesForDropdown();

    /**
     * Client: Lấy danh sách chuyên mục ACTIVE.
     */
    ApiResponse<List<CategoryResponse>> getClientActiveCategories();

    /**
     * Lấy chi tiết chuyên mục theo ID.
     */
    ApiResponse<CategoryResponse> getCategoryById(Long id);

    /**
     * Admin: Tạo chuyên mục mới (nếu có file ảnh thì tự động upload lên Cloudinary FashionShop2/categories).
     */
    ApiResponse<CategoryResponse> createCategory(CategoryRequest request, MultipartFile imageFile);

    /**
     * Admin: Cập nhật chuyên mục (nếu có file ảnh mới thì upload Cloudinary và cập nhật imageUrl).
     */
    ApiResponse<CategoryResponse> updateCategory(Long id, CategoryRequest request, MultipartFile imageFile);

    /**
     * Admin: Xóa mềm chuyên mục.
     */
    ApiResponse<Void> deleteCategory(Long id);
}
