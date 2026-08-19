package com.ec01.vo.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuVO {
    private Long id;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private Byte status;
}
