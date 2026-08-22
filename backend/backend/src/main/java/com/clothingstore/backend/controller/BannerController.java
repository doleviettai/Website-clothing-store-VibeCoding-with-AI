package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.BannerResponse;
import com.clothingstore.backend.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Banner công khai (Public / Client).
 */
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * GET /api/v1/banners?position=HOME_TOP
     * Lấy danh sách banner đang hoạt động (ACTIVE) theo vị trí hiển thị (sắp xếp thứ tự sortOrder).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getClientBanners(
            @RequestParam(required = false) String position
    ) {
        return ResponseEntity.ok(bannerService.getClientBannersByPosition(position));
    }

    /**
     * GET /api/v1/banners/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerResponse>> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }
}
