package com.clothingstore.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class MoMoConfig {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:F8BBA84260B7E544}")
    private String accessKey;

    @Value("${momo.secret-key:K951B6E68B129F1E801545881D85703F}")
    private String secretKey;

    @Value("${momo.create-order-url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String createOrderUrl;

    @Value("${momo.query-order-url:https://test-payment.momo.vn/v2/gateway/api/query}")
    private String queryOrderUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/v1/payments/momo/ipn}")
    private String ipnUrl;

    @Value("${momo.redirect-url:http://localhost:5173/payment-result}")
    private String redirectUrl;
}
