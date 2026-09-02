package com.clothingstore.backend.controller;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.DashboardSummaryResponse;
import com.clothingstore.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Thống Kê Báo Cáo Dashboard Cho Admin.
 * Base path: /api/v1/admin/dashboard
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * GET /api/v1/admin/dashboard/summary?days=7
     * Trả về 4 chỉ số thống kê (Doanh Thu, Đơn Hàng, Sản Phẩm Trong Kho, Khách Hàng)
     * và Dữ liệu Đồ Thị Cột Trục X (Ngày) & Trục Y (Tổng Doanh Thu Đơn Hàng).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(adminDashboardService.getDashboardSummary(days));
    }
}
