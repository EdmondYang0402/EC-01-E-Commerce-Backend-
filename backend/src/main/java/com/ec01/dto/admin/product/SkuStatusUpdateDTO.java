package com.ec01.dto.admin.product;

import com.ec01.common.SkuStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkuStatusUpdateDTO {
    @NotNull(message = "SKU状态不能为空")
    private SkuStatus status;
}
