package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private String customerName;

    private String transactionCode;
    private String gatewayTransactionNo;
    private String paymentGateway;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentInfo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
