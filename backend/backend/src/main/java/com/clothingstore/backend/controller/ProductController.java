package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.ProductResponse;
import com.clothingstore.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Sản phẩm công khai (Public / Client).
 * Base path: /api/v1/products
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * Lấy danh sách sản phẩm cửa hàng phân trang, tìm kiếm realtime, lọc theo chuyên mục, thương hiệu và sắp xếp.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getClientProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        return ResponseEntity.ok(productService.getClientProducts(keyword, categoryId, brandId, sort, page, size));
    }

    /**
     * GET /api/v1/products/all
     * Lấy danh sách tất cả sản phẩm active.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        return ResponseEntity.ok(productService.getClientActiveProducts());
    }

    /**
     * GET /api/v1/products/{id}
     * Lấy chi tiết sản phẩm.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
