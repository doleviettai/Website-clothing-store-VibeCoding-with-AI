package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoMoCreateRequest {

    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;
}
