package com.clothingstore.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class ZaloPayConfig {

    @Value("${zalopay.app-id:2553}")
    private String appId;

    @Value("${zalopay.key1:SdngBKC0YTaIhhvStvFrHasRwOxWZImw}")
    private String key1;

    @Value("${zalopay.key2:trL0aYVssSonhBriefoAfAz0n2WnAohq}")
    private String key2;

    @Value("${zalopay.create-order-url:https://sb-openapi.zalopay.vn/v2/create}")
    private String createOrderUrl;

    @Value("${zalopay.query-order-url:https://sb-openapi.zalopay.vn/v2/query}")
    private String queryOrderUrl;

    @Value("${zalopay.callback-url:http://localhost:8080/api/v1/payments/zalopay/callback}")
    private String callbackUrl;

    @Value("${zalopay.redirect-url:http://localhost:5173/payment-result}")
    private String redirectUrl;
}
