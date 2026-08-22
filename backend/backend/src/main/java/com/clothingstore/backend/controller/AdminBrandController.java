package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.BrandRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BrandResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.service.BrandService;
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
 * Controller Admin cho Quản lý Thương hiệu (Full CRUD + Multipart upload logo Cloudinary).
 * Base path: /api/v1/admin/brands
 */
@RestController
@RequestMapping("/api/v1/admin/brands")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBrandController {

    private final BrandService brandService;

    /**
     * GET /api/v1/admin/brands
     * Lấy danh sách thương hiệu phân trang & tìm kiếm.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BrandResponse>>> getAdminBrands(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(brandService.getAdminBrands(keyword, status, page, size));
    }

    /**
     * GET /api/v1/admin/brands/dropdown
     */
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getDropdownBrands() {
        return ResponseEntity.ok(brandService.getAllBrandsForDropdown());
    }

    /**
     * GET /api/v1/admin/brands/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    /**
     * POST /api/v1/admin/brands
     * Tạo thương hiệu mới (Multipart/form-data kèm file logo Cloudinary).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestPart("brand") BrandRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<BrandResponse> response = brandService.createBrand(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/admin/brands/{id}
     * Cập nhật thương hiệu.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestPart("brand") BrandRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<BrandResponse> response = brandService.updateBrand(id, request, imageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/brands/{id}
     * Xóa mềm thương hiệu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.deleteBrand(id));
    }
}
