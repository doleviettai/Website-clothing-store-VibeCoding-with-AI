package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.request.MoMoCreateRequest;
import com.clothingstore.backend.dto.request.MoMoIPNRequest;
import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.MoMoCreateResponse;

import java.util.Map;

public interface MoMoService {

    ApiResponse<MoMoCreateResponse> createPayment(String userEmail, MoMoCreateRequest request);

    Map<String, Object> handleIPN(MoMoIPNRequest ipnRequest);

    ApiResponse<Map<String, Object>> getPaymentStatusByOrder(Long orderId);

    ApiResponse<Map<String, Object>> confirmMockPayment(Long orderId, String requestId, String status);
}
