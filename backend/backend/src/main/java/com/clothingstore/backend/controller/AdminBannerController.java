package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.BannerRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BannerResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller Admin cho Quản lý Banner (Full CRUD + Multipart upload ảnh Cloudinary).
 * Base path: /api/v1/admin/banners
 */
@RestController
@RequestMapping("/api/v1/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    /**
     * GET /api/v1/admin/banners
     * Lấy danh sách banner phân trang, tìm kiếm & lọc theo vị trí position.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BannerResponse>>> getAdminBanners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bannerService.getAdminBanners(keyword, position, status, page, size));
    }

    /**
     * GET /api/v1/admin/banners/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    /**
     * POST /api/v1/admin/banners
     * Tạo banner mới (Multipart/form-data kèm file ảnh upload Cloudinary).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BannerResponse>> createBanner(
            @Valid @RequestPart("banner") BannerRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<BannerResponse> response = bannerService.createBanner(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/admin/banners/{id}
     * Cập nhật banner.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BannerResponse>> updateBanner(
            @PathVariable Long id,
            @Valid @RequestPart("banner") BannerRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<BannerResponse> response = bannerService.updateBanner(id, request, imageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/banners/{id}
     * Xóa mềm banner.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.deleteBanner(id));
    }
}
