package com.ec01.dto.admin.product;

import com.ec01.common.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateDTO {
    @NotNull(message = "商品状态不能为空")
    private ProductStatus status;
}
