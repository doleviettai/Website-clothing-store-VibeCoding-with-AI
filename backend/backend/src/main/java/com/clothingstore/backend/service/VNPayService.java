package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.VNPayCreateRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.VNPayCreateResponse;

import java.util.Map;

public interface VNPayService {

    ApiResponse<VNPayCreateResponse> createPayment(String userEmail, VNPayCreateRequest request);

    ApiResponse<Map<String, Object>> getPaymentStatusByOrder(Long orderId);

    ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String txnRef, String status);
}
