package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Sku {
    private Long id;
    private Long productId;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private Byte status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
