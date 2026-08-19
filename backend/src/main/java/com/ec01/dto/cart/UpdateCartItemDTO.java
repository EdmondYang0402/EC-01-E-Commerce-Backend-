package com.ec01.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemDTO {
    @Min(1)
    private Integer quantity;
    @Min(0)
    @Max(1)
    private Byte selected;
}
