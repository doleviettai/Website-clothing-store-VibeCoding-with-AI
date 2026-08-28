package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;
}
