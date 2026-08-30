package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.CheckoutRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    ApiResponse<OrderResponse> checkout(String userEmail, CheckoutRequest request);

    ApiResponse<List<OrderResponse>> getUserOrders(String userEmail);

    ApiResponse<OrderResponse> getUserOrderDetail(String userEmail, Long orderId);

    ApiResponse<OrderResponse> cancelUserOrder(String userEmail, Long orderId);
}
