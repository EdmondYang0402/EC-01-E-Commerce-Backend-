package com.ec01.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface AlipayGateway {
    String createPagePayment(String orderNo, BigDecimal amount);

    void verifyNotification(Map<String, String> params);
}
