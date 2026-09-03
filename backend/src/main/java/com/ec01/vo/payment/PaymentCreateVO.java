package com.ec01.vo.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentCreateVO extends PaymentVO {
    private String paymentForm;
}
