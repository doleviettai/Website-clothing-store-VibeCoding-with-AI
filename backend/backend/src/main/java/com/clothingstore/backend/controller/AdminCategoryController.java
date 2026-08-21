package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.CategoryRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.CategoryResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller Admin cho Quản lý Chuyên mục (Full CRUD + File upload lên Cloudinary).
 * Base path: /api/v1/admin/categories
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/v1/admin/categories
     * Lấy danh sách chuyên mục phân trang, tìm kiếm & lọc.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getAdminCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(categoryService.getAdminCategories(keyword, status, parentId, page, size));
    }

    /**
     * GET /api/v1/admin/categories/dropdown
     * Lấy tất cả chuyên mục làm danh sách chọn chuyên mục cha.
     */
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getDropdownCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesForDropdown());
    }

    /**
     * GET /api/v1/admin/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * POST /api/v1/admin/categories
     * Tạo chuyên mục mới (hỗ trợ multipart/form-data kèm file ảnh upload Cloudinary).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestPart("category") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<CategoryResponse> response = categoryService.createCategory(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/admin/categories/{id}
     * Cập nhật chuyên mục.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestPart("category") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<CategoryResponse> response = categoryService.updateCategory(id, request, imageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/categories/{id}
     * Xóa mềm chuyên mục.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
