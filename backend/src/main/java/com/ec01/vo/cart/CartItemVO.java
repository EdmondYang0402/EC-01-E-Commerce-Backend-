package com.ec01.vo.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String coverUrl;
    private Long skuId;
    private String specJson;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Byte selected;
}
