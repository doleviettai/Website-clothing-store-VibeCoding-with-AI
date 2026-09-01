package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VNPayCreateResponse {

    private boolean success;
    private String paymentUrl;
    private String txnRef;
    private String message;
}
