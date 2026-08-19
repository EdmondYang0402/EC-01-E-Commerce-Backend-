package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CartItem {
    private Long id;
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Byte selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
