package com.ec01.vo.admin.product;

import com.ec01.common.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminProductListVO {
    private Long id;
    private String name;
    private String subtitle;
    private String coverUrl;
    private ProductStatus status;
    private Long categoryId;
    private BigDecimal minPrice;
    private Integer skuCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
