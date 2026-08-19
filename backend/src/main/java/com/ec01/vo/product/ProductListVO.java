package com.ec01.vo.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListVO {
    private Long id;
    private String name;
    private String subtitle;
    private String coverUrl;
    private BigDecimal minPrice;
    private Byte status;
}
