package com.ec01.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Payment {
    private Long id;
    private Long orderId;
    private String paymentNo;
    private String provider;
    private String providerTradeNo;
    private BigDecimal amount;
    private Byte status;
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
