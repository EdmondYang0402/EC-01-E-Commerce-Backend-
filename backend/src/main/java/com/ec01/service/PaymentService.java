package com.ec01.service;

import com.ec01.vo.payment.PaymentCreateVO;
import com.ec01.vo.payment.PaymentVO;

import java.util.Map;

public interface PaymentService {
    PaymentCreateVO createPayment(String orderNo);

    void handleAlipayNotify(Map<String, String> params);

    PaymentVO getPaymentByOrderNo(String orderNo);
}
