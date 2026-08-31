package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MoMoCreateResponse {

    private boolean success;
    private String payUrl;
    private String requestId;
    private String orderId;
    private String message;
    private Integer resultCode;
}
