package com.ec01.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {
    @NotEmpty
    private List<Long> cartItemIds;

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    @Size(max = 30, message = "收货电话不能超过30个字符")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 500, message = "收货地址不能超过500个字符")
    private String receiverAddress;
}
