package com.clothingstore.backend.service.impl;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.DailyRevenueDTO;
import com.clothingstore.backend.dto.response.DashboardSummaryResponse;
import com.clothingstore.backend.entity.Order;
import com.clothingstore.backend.entity.Product;
import com.clothingstore.backend.repository.OrderRepository;
import com.clothingstore.backend.repository.ProductRepository;
import com.clothingstore.backend.repository.UserRepository;
import com.clothingstore.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary(int days) {
        if (days <= 0) {
            days = 7;
        }

        // 1. Thống kê Doanh Thu Tích Lũy (Total Revenue)
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Tổng đơn hàng (Total Orders)
        long totalOrders = allOrders.size();

        // 3. Tổng sản phẩm trong kho (Total Stock Quantity)
        List<Product> allProducts = productRepository.findAll();
        long totalProductsInStock = allProducts.stream()
                .filter(p -> p.getDeletedAt() == null)
                .mapToLong(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                .sum();

        // 4. Số lượng khách hàng (Total Customers)
        long totalCustomers = userRepository.count();

        // 5. Tính toán dữ liệu đồ thị cột theo ngày (Daily Revenue Chart Data)
        Map<String, BigDecimal> dailyRevenueMap = new LinkedHashMap<>();
        Map<String, Long> dailyOrderCountMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();

        // Tạo danh sách các ngày trong `days` vừa qua
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            dailyRevenueMap.put(dateStr, BigDecimal.ZERO);
            dailyOrderCountMap.put(dateStr, 0L);
        }

        // Nhóm các đơn hàng thực tế vào ngày
        for (Order order : allOrders) {
            if ("CANCELLED".equalsIgnoreCase(order.getStatus()) || order.getCreatedAt() == null) {
                continue;
            }
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            String dateStr = orderDate.format(formatter);

            if (dailyRevenueMap.containsKey(dateStr)) {
                BigDecimal currentRev = dailyRevenueMap.get(dateStr);
                dailyRevenueMap.put(dateStr, currentRev.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO));
                dailyOrderCountMap.put(dateStr, dailyOrderCountMap.get(dateStr) + 1);
            }
        }

        List<DailyRevenueDTO> dailyRevenues = new ArrayList<>();
        for (String dateStr : dailyRevenueMap.keySet()) {
            dailyRevenues.add(DailyRevenueDTO.builder()
                    .date(dateStr)
                    .totalRevenue(dailyRevenueMap.get(dateStr))
                    .orderCount(dailyOrderCountMap.get(dateStr))
                    .build());
        }

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProductsInStock)
                .totalCustomers(totalCustomers)
                .dailyRevenues(dailyRevenues)
                .build();

        return ApiResponse.success("Lấy báo cáo dashboard thành công", summary);
    }
}
