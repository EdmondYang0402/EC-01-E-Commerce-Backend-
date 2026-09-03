package com.ec01.service.impl;

import com.ec01.common.OrderStatus;
import com.ec01.common.PaymentStatus;
import com.ec01.entity.Order;
import com.ec01.entity.Payment;
import com.ec01.exception.BusinessException;
import com.ec01.exception.PaymentNotificationException;
import com.ec01.mapper.OrderMapper;
import com.ec01.mapper.PaymentMapper;
import com.ec01.payment.AlipayGateway;
import com.ec01.security.UserContext;
import com.ec01.service.PaymentService;
import com.ec01.vo.payment.PaymentCreateVO;
import com.ec01.vo.payment.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private static final String PROVIDER = "ALIPAY";
    private static final DateTimeFormatter ALIPAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final AlipayGateway alipayGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentCreateVO createPayment(String orderNo) {
        Long userId = requireUser();
        String normalizedOrderNo = requireOrderNo(orderNo);
        Order order = orderMapper.selectByOrderNoAndUserIdForUpdate(normalizedOrderNo, userId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在或无权操作");
        }
        if (order.getStatus() == OrderStatus.CANCELLED.getCode()) {
            throw new BusinessException("已取消订单不能支付");
        }
        if (order.getStatus() == OrderStatus.PAID.getCode()) {
            throw new BusinessException("订单已经支付");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new BusinessException("当前订单状态不能支付");
        }

        Payment payment = paymentMapper.selectByOrderIdAndStatusForUpdate(
                order.getId(), PaymentStatus.PENDING.getCode());
        if (payment == null) {
            payment = newPayment(order);
            if (paymentMapper.insert(payment) != 1) {
                throw new BusinessException("支付记录创建失败");
            }
        } else if (payment.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new BusinessException(500, "支付记录金额与订单不一致");
        }

        String paymentForm = alipayGateway.createPagePayment(order.getOrderNo(), payment.getAmount());
        PaymentCreateVO vo = new PaymentCreateVO();
        copy(payment, vo);
        vo.setPaymentForm(paymentForm);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new PaymentNotificationException("Empty Alipay notification");
        }
        alipayGateway.verifyNotification(params);

        String merchantTradeNo = requiredParam(params, "out_trade_no");
        String providerTradeNo = requiredParam(params, "trade_no");
        String tradeStatus = requiredParam(params, "trade_status");
        BigDecimal notifiedAmount = parseAmount(requiredParam(params, "total_amount"));

        Order order = orderMapper.selectByOrderNoForUpdate(merchantTradeNo);
        Payment payment;
        if (order != null) {
            payment = paymentMapper.selectLatestByOrderIdForUpdate(order.getId());
        } else {
            // Payment forms created before orderNo became out_trade_no used paymentNo.
            payment = paymentMapper.selectByPaymentNoForUpdate(merchantTradeNo);
            if (payment == null) {
                throw new PaymentNotificationException("Payment not found");
            }
            order = orderMapper.selectByIdForUpdate(payment.getOrderId());
        }
        if (payment == null || !PROVIDER.equals(payment.getProvider())) {
            throw new PaymentNotificationException("Payment not found");
        }
        if (order == null || !order.getId().equals(payment.getOrderId())) {
            throw new PaymentNotificationException("Order not found");
        }
        if (payment.getAmount().compareTo(notifiedAmount) != 0
                || order.getTotalAmount().compareTo(notifiedAmount) != 0) {
            throw new PaymentNotificationException("Payment amount mismatch");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS.getCode()) {
            if (!providerTradeNo.equals(payment.getProviderTradeNo())
                    || order.getStatus() != OrderStatus.PAID.getCode()) {
                throw new PaymentNotificationException("Inconsistent duplicate notification");
            }
            log.info("Duplicate Alipay success notification ignored: orderNo={}", order.getOrderNo());
            return;
        }

        if ("TRADE_CLOSED".equals(tradeStatus)) {
            paymentMapper.updateStatus(payment.getId(), PaymentStatus.PENDING.getCode(),
                    PaymentStatus.FAILED.getCode(), providerTradeNo, null);
            log.info("Alipay trade closed: orderNo={}, providerTradeNo={}", order.getOrderNo(), providerTradeNo);
            return;
        }
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            throw new PaymentNotificationException("Unsupported Alipay trade status");
        }
        if (payment.getStatus() != PaymentStatus.PENDING.getCode()) {
            throw new PaymentNotificationException("Payment status cannot become successful");
        }

        LocalDateTime paidTime = parsePaidTime(params.get("gmt_payment"));
        int paymentUpdated = paymentMapper.updateStatus(
                payment.getId(), PaymentStatus.PENDING.getCode(), PaymentStatus.SUCCESS.getCode(),
                providerTradeNo, paidTime);
        if (paymentUpdated != 1) {
            throw new PaymentNotificationException("Payment status changed concurrently");
        }
        int orderUpdated = orderMapper.markPaid(
                order.getId(), OrderStatus.PENDING_PAYMENT.getCode(), OrderStatus.PAID.getCode(), paidTime);
        if (orderUpdated != 1) {
            throw new PaymentNotificationException("Order status changed concurrently");
        }
        log.info("Alipay payment succeeded: orderNo={}, providerTradeNo={}",
                order.getOrderNo(), providerTradeNo);
    }

    @Override
    public PaymentVO getPaymentByOrderNo(String orderNo) {
        Long userId = requireUser();
        Order order = orderMapper.selectByOrderNoAndUserId(requireOrderNo(orderNo), userId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在或无权查看");
        }
        Payment payment = paymentMapper.selectLatestByOrderId(order.getId());
        if (payment == null) {
            throw new BusinessException(404, "该订单暂无支付记录");
        }
        PaymentVO vo = new PaymentVO();
        copy(payment, vo);
        return vo;
    }

    private Payment newPayment(Order order) {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPaymentNo("PAY" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        payment.setProvider(PROVIDER);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        return payment;
    }

    private void copy(Payment payment, PaymentVO vo) {
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setProvider(payment.getProvider());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setPaidTime(payment.getPaidTime());
    }

    private Long requireUser() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return userId;
    }

    private String requireOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException(400, "订单号不能为空");
        }
        return orderNo.trim();
    }

    private String requiredParam(Map<String, String> params, String name) {
        String value = params.get(name);
        if (!StringUtils.hasText(value)) {
            throw new PaymentNotificationException("Missing Alipay notification field: " + name);
        }
        return value;
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new PaymentNotificationException("Invalid Alipay amount");
        }
    }

    private LocalDateTime parsePaidTime(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(value, ALIPAY_TIME);
        } catch (DateTimeParseException exception) {
            throw new PaymentNotificationException("Invalid Alipay payment time");
        }
    }
}
