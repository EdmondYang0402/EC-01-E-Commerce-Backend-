package com.ec01.dto.admin.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuCreateDTO {
    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 64, message = "SKU编码不能超过64个字符")
    private String skuCode;

    @NotBlank(message = "SKU规格不能为空")
    @Size(max = 500, message = "SKU规格不能超过500个字符")
    private String specJson;

    @NotNull(message = "SKU价格不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "SKU价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "SKU库存不能为负数")
    private Integer stock;
}
