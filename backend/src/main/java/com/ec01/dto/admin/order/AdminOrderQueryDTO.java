package com.ec01.dto.admin.order;

import com.ec01.common.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminOrderQueryDTO {
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 20;

    @Size(max = 64, message = "订单号不能超过64个字符")
    private String orderNo;

    private OrderStatus status;

    @Positive(message = "用户ID必须为正数")
    private Long userId;
}
