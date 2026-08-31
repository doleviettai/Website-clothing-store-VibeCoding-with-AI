package com.clothingstore.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoMoIPNRequest {

    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private String transId;
    private Integer resultCode;
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;
}
