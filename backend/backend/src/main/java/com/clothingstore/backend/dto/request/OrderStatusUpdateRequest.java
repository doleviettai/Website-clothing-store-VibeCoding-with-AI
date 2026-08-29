package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    private String status;
}
