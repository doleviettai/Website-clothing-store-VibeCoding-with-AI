package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.ProductResponse;
import com.clothingstore.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Sản phẩm công khai (Public / Client).
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * Lấy danh sách sản phẩm đang hoạt động (ACTIVE).
     */
    @GetMapping
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
