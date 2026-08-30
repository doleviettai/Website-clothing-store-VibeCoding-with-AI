package com.clothingstore.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @NotBlank(message = "Họ và tên người nhận không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String email;

    @NotBlank(message = "Tỉnh / Thành phố không được để trống")
    private String province;

    private String district;

    @NotBlank(message = "Phường / Xã không được để trống")
    private String ward;

    @NotBlank(message = "Số nhà / Thôn / Tên đường không được để trống")
    private String streetAddress;

    private String note;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod = "COD";
}
