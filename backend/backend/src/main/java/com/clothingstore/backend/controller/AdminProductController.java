package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.request.ProductRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductResponse;
import com.clothingstore.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller Admin cho Quản lý Sản phẩm (Full CRUD + Multipart upload ảnh Cloudinary).
 * Base path: /api/v1/admin/products
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/admin/products
     * Lấy danh sách sản phẩm phân trang, tìm kiếm & lọc theo chuyên mục, thương hiệu, trạng thái.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAdminProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getAdminProducts(keyword, categoryId, brandId, status, page, size));
    }

    /**
     * GET /api/v1/admin/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * POST /api/v1/admin/products
     * Tạo sản phẩm mới (Multipart/form-data chọn ảnh upload Cloudinary).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<ProductResponse> response = productService.createProduct(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/admin/products/{id}
     * Cập nhật sản phẩm.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        ApiResponse<ProductResponse> response = productService.updateProduct(id, request, imageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/admin/products/{id}
     * Xóa mềm sản phẩm.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
}
