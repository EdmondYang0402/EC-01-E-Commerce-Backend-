package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long skuId;
    private Long productId;
    private String productName;
    private String skuSpec;
    private String coverUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime createTime;
}
