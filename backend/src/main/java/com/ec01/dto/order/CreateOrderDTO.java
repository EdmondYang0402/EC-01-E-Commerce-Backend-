package com.ec01.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {
    @NotEmpty
    private List<Long> cartItemIds;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}
