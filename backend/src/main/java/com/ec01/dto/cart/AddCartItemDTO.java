package com.ec01.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemDTO {
    @NotNull
    private Long skuId;
    @NotNull
    @Min(1)
    private Integer quantity;
}
