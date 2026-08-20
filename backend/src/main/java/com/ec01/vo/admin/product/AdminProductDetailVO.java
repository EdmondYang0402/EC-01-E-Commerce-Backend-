package com.ec01.vo.admin.product;

import com.ec01.common.ProductStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminProductDetailVO {
    private Long id;
    private String name;
    private String subtitle;
    private String coverUrl;
    private String description;
    private Long categoryId;
    private ProductStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AdminSkuVO> skus;
}
