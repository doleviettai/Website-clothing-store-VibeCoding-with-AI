package com.clothingstore.backend.service;

import com.clothingstore.backend.dto.response.ApiResponse;
import com.clothingstore.backend.dto.response.PageResponse;
import com.clothingstore.backend.dto.response.PaymentResponse;

public interface AdminPaymentService {

    ApiResponse<PageResponse<PaymentResponse>> getPayments(
            String keyword, String paymentGateway, String status, int page, int size
    );

    ApiResponse<PaymentResponse> getPaymentDetail(Long paymentId);
}
