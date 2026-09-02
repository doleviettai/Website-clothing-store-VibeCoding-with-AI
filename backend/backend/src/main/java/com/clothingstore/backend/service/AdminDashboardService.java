package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.DashboardSummaryResponse;

public interface AdminDashboardService {
    ApiResponse<DashboardSummaryResponse> getDashboardSummary(int days);
}
