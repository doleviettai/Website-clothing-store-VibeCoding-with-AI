package com.clothingstore.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZaloPayCreateResponse {

    private boolean success;
    private String orderUrl;
    private String appTransId;
    private String message;
    private Integer returnCode;
}
