package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.ZaloPayCallbackRequest;
import com.clothingstore.backend.dto.request.ZaloPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.ZaloPayCreateResponse;

import java.util.Map;

public interface ZaloPayService {

    ApiResponse<ZaloPayCreateResponse> createPayment(String userEmail, ZaloPayCreateRequest request);

    Map<String, Object> handleCallback(ZaloPayCallbackRequest callbackRequest);

    ApiResponse<Map<String, Object>> getPaymentStatusByOrder(Long orderId);

    ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String appTransId, String status);
}
