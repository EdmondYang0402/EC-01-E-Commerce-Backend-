package com.ec01.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.ec01.config.AlipayProperties;
import com.ec01.exception.BusinessException;
import com.ec01.exception.PaymentNotificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OfficialAlipayGateway implements AlipayGateway {
    private final ObjectProvider<AlipayClient> clientProvider;
    private final AlipayProperties properties;

    @Override
    public String createPagePayment(String orderNo, BigDecimal amount) {
        AlipayClient client = clientProvider.getIfAvailable();
        if (!properties.isEnabled() || client == null) {
            throw new BusinessException(503, "支付宝沙箱支付尚未配置");
        }

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount(amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString());
        model.setSubject("EC-01 订单 " + orderNo);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setReturnUrl(UriComponentsBuilder.fromUriString(properties.getReturnUrl())
                .queryParam("orderNo", orderNo)
                .build()
                .encode()
                .toUriString());

        try {
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (response == null || !StringUtils.hasText(response.getBody())) {
                throw new BusinessException(502, "支付宝支付页面生成失败");
            }
            return response.getBody();
        } catch (AlipayApiException exception) {
            throw new BusinessException(502, "支付宝支付请求失败");
        }
    }

    @Override
    public void verifyNotification(Map<String, String> params) {
        if (!properties.isEnabled() || !StringUtils.hasText(properties.getAlipayPublicKey())) {
            throw new PaymentNotificationException("Alipay sandbox is not configured");
        }
        try {
            boolean valid = AlipaySignature.rsaCheckV1(
                    params, properties.getAlipayPublicKey(), "UTF-8", "RSA2");
            if (!valid) {
                throw new PaymentNotificationException("Alipay signature verification failed");
            }
        } catch (AlipayApiException exception) {
            throw new PaymentNotificationException("Alipay signature verification failed");
        }

        if (!properties.getAppId().equals(params.get("app_id"))) {
            throw new PaymentNotificationException("Alipay app_id mismatch");
        }
        if (StringUtils.hasText(properties.getSellerId())
                && !properties.getSellerId().equals(params.get("seller_id"))) {
            throw new PaymentNotificationException("Alipay seller_id mismatch");
        }
    }
}
