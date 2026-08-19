package com.ec01.dto.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductQueryDTO {
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 20;

    @Size(max = 120, message = "搜索关键词不能超过120个字符")
    private String keyword;

    @Positive(message = "分类ID必须为正数")
    private Long categoryId;
}
