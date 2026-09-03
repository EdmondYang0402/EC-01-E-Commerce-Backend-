package com.ec01.vo.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private String paymentNo;
    private String provider;
    private BigDecimal amount;
    private Byte status;
    private LocalDateTime paidTime;
}
