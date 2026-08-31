package com.clothingstore.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZaloPayCallbackRequest {

    private String data;
    private String mac;
    private Integer type;
}
