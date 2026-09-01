package com.clothingstore.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class VNPayConfig {

    @Value("${vnpay.tmn-code:CGXZ24M1}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:XTYUIOPQASDFGHJKL1234567890MNBVC}")
    private String hashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.return-url:http://localhost:5173/payment-result}")
    private String returnUrl;
}
