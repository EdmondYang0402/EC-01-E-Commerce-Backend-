package com.ec01.dto.category;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateDTO {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 80, message = "分类名称不能超过80个字符")
    private String name;

    @NotNull(message = "分类排序不能为空")
    @Min(value = 0, message = "分类排序不能小于0")
    @Max(value = 9999, message = "分类排序不能大于9999")
    private Integer sortOrder;
}
