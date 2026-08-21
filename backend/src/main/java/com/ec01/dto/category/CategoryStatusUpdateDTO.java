package com.ec01.dto.category;

import com.ec01.common.CategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryStatusUpdateDTO {
    @NotNull(message = "分类状态不能为空")
    private CategoryStatus status;
}
