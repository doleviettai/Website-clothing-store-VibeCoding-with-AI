package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.OrderStatusUpdateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderResponse;
import com.clothingstore.backend.dto.response.PageResponse;

public interface AdminOrderService {

    ApiResponse<PageResponse<OrderResponse>> getOrders(
            String keyword, String status, String paymentStatus, int page, int size
    );

    ApiResponse<OrderResponse> getOrderDetail(Long orderId);

    ApiResponse<OrderResponse> updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    ApiResponse<OrderResponse> updatePaymentStatus(Long orderId, String paymentStatus);
}
