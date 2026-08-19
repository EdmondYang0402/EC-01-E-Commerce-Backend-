package com.ec01.vo.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuSpec;
    private String coverUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
