package com.ec01.dto.admin.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 120, message = "商品名称不能超过120个字符")
    private String name;

    @Size(max = 255, message = "商品副标题不能超过255个字符")
    private String subtitle;

    @Size(max = 500, message = "商品封面地址不能超过500个字符")
    private String coverUrl;

    private String description;

    @NotNull(message = "二级分类不能为空")
    @Positive(message = "分类ID必须为正数")
    private Long categoryId;
}
