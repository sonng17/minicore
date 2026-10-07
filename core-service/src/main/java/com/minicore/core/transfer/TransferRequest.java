package com.minicore.core.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Body của POST /v1/transfers. Kiểm tra định dạng ở đây; quy tắc nghiệp vụ nằm trong PKG_TRANSFER. */
public record TransferRequest(
        @NotBlank(message = "không được để trống")
        @Size(max = 20, message = "tối đa 20 ký tự")
        String fromAccount,

        @NotBlank(message = "không được để trống")
        @Size(max = 20, message = "tối đa 20 ký tự")
        String toAccount,

        @NotNull(message = "không được để trống")
        @DecimalMin(value = "0", inclusive = false, message = "phải lớn hơn 0")
        @Digits(integer = 17, fraction = 2, message = "tối đa 2 chữ số thập phân")
        BigDecimal amount,

        @NotNull(message = "phải là TELLER, DIGITAL hoặc PARTNER")
        Channel channel,

        @Size(max = 500, message = "tối đa 500 ký tự")
        String description
) {
}
