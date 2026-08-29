package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private String orderCode;
    private Long userId;
    private String userEmail;

    // Thông tin giao nhận
    private String customerName;
    private String phone;
    private String email;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String fullAddress;
    private String note;

    // Tiền tệ & Thanh toán
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;

    // Trạng thái đơn hàng
    private String status;

    // Danh sách sản phẩm trong đơn
    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
