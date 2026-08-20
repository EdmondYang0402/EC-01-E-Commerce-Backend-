package com.ec01.vo.admin.product;

import com.ec01.common.SkuStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminSkuVO {
    private Long id;
    private Long productId;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private SkuStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
