package com.clothingstore.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    // 4 Stat Cards
    private BigDecimal totalRevenue;   // Card 1: Doanh Thu Tích Lũy
    private Long totalOrders;          // Card 2: Tổng Đơn Hàng
    private Long totalProducts;        // Card 3: Tổng Sản Phẩm Trong Kho
    private Long totalCustomers;       // Card 4: Số Lượng Khách Hàng

    // Biểu đồ cột trục X (ngày) & Y (doanh thu)
    private List<DailyRevenueDTO> dailyRevenues;
}
