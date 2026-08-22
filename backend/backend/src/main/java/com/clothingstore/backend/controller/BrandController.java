package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BrandResponse;
import com.clothingstore.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Thương hiệu công khai (Public / Client).
 */
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * GET /api/v1/brands
     * Lấy danh sách thương hiệu đang hoạt động (ACTIVE).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getActiveBrands() {
        return ResponseEntity.ok(brandService.getClientActiveBrands());
    }

    /**
     * GET /api/v1/brands/{id}
     * Lấy chi tiết thương hiệu.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }
}
