package com.ec01.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Byte status;
    private BigDecimal totalAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}
