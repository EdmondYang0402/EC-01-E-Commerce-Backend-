package com.ec01.service;

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
import com.ec01.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {
    private OrderMapper orderMapper;
    private PaymentMapper paymentMapper;
    private AlipayGateway alipayGateway;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        paymentMapper = mock(PaymentMapper.class);
        alipayGateway = mock(AlipayGateway.class);
        service = new PaymentServiceImpl(orderMapper, paymentMapper, alipayGateway);
        UserContext.set(42L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void pendingOrderCreatesPendingAlipayPaymentFromDatabaseAmount() {
        Order order = order(OrderStatus.PENDING_PAYMENT, "299.00");
        when(orderMapper.selectByOrderNoAndUserIdForUpdate("EC001", 42L)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            invocation.<Payment>getArgument(0).setId(7L);
            return 1;
        });
        when(alipayGateway.createPagePayment(eq("EC001"), eq(new BigDecimal("299.00"))))
                .thenReturn("<form>pay</form>");

        var result = service.createPayment(" EC001 ");

        assertEquals(new BigDecimal("299.00"), result.getAmount());
        assertEquals(PaymentStatus.PENDING.getCode(), result.getStatus());
        assertEquals("<form>pay</form>", result.getPaymentForm());
        verify(paymentMapper).insert(any(Payment.class));
    }

    @Test
    void successfulNotifyUsesCasForPaymentAndOrder() {
        Payment payment = payment(PaymentStatus.PENDING, "299.00");
        Order order = order(OrderStatus.PENDING_PAYMENT, "299.00");
        when(orderMapper.selectByOrderNoForUpdate("EC001")).thenReturn(order);
        when(paymentMapper.selectLatestByOrderIdForUpdate(9L)).thenReturn(payment);
        when(paymentMapper.updateStatus(eq(7L), eq((byte) 0), eq((byte) 1), eq("ALI001"), any()))
                .thenReturn(1);
        when(orderMapper.markPaid(eq(9L), eq((byte) 0), eq((byte) 1), any())).thenReturn(1);

        service.handleAlipayNotify(successNotify("299.00"));

        verify(alipayGateway).verifyNotification(any());
        verify(paymentMapper).updateStatus(eq(7L), eq((byte) 0), eq((byte) 1), eq("ALI001"), any());
        verify(orderMapper).markPaid(eq(9L), eq((byte) 0), eq((byte) 1), any());
    }

    @Test
    void duplicateSuccessfulNotifyIsIdempotent() {
        Payment payment = payment(PaymentStatus.SUCCESS, "299.00");
        payment.setProviderTradeNo("ALI001");
        Order order = order(OrderStatus.PAID, "299.00");
        when(orderMapper.selectByOrderNoForUpdate("EC001")).thenReturn(order);
        when(paymentMapper.selectLatestByOrderIdForUpdate(9L)).thenReturn(payment);

        service.handleAlipayNotify(successNotify("299.00"));

        verify(paymentMapper, never()).updateStatus(any(), any(), any(), any(), any());
        verify(orderMapper, never()).markPaid(any(), any(), any(), any());
    }

    @Test
    void mismatchedAmountIsRejected() {
        when(orderMapper.selectByOrderNoForUpdate("EC001"))
                .thenReturn(order(OrderStatus.PENDING_PAYMENT, "299.00"));
        when(paymentMapper.selectLatestByOrderIdForUpdate(9L))
                .thenReturn(payment(PaymentStatus.PENDING, "299.00"));

        assertThrows(PaymentNotificationException.class,
                () -> service.handleAlipayNotify(successNotify("1.00")));
        verify(paymentMapper, never()).updateStatus(any(), any(), any(), any(), any());
    }

    @Test
    void invalidSignatureIsRejectedBeforeDatabaseAccess() {
        Map<String, String> params = successNotify("299.00");
        doThrow(new PaymentNotificationException("bad signature"))
                .when(alipayGateway).verifyNotification(params);

        assertThrows(PaymentNotificationException.class, () -> service.handleAlipayNotify(params));
        verifyNoInteractions(orderMapper, paymentMapper);
    }

    @Test
    void cancelledAndPaidOrdersCannotCreatePayment() {
        when(orderMapper.selectByOrderNoAndUserIdForUpdate("CANCELLED", 42L))
                .thenReturn(order(OrderStatus.CANCELLED, "10.00"));
        when(orderMapper.selectByOrderNoAndUserIdForUpdate("PAID", 42L))
                .thenReturn(order(OrderStatus.PAID, "10.00"));

        assertThrows(BusinessException.class, () -> service.createPayment("CANCELLED"));
        assertThrows(BusinessException.class, () -> service.createPayment("PAID"));
        verifyNoInteractions(paymentMapper, alipayGateway);
    }

    @Test
    void cancelWinningRacePreventsIllegalPaidOverwrite() {
        when(orderMapper.selectByOrderNoForUpdate("EC001"))
                .thenReturn(order(OrderStatus.PENDING_PAYMENT, "299.00"));
        when(paymentMapper.selectLatestByOrderIdForUpdate(9L))
                .thenReturn(payment(PaymentStatus.PENDING, "299.00"));
        when(paymentMapper.updateStatus(eq(7L), eq((byte) 0), eq((byte) 1), eq("ALI001"), any()))
                .thenReturn(1);
        when(orderMapper.markPaid(eq(9L), eq((byte) 0), eq((byte) 1), any())).thenReturn(0);

        assertThrows(PaymentNotificationException.class,
                () -> service.handleAlipayNotify(successNotify("299.00")));
    }

    @Test
    void legacyPaymentNumberNotificationRemainsSupported() {
        Payment payment = payment(PaymentStatus.PENDING, "299.00");
        Order order = order(OrderStatus.PENDING_PAYMENT, "299.00");
        when(orderMapper.selectByOrderNoForUpdate("PAY001")).thenReturn(null);
        when(paymentMapper.selectByPaymentNoForUpdate("PAY001")).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(9L)).thenReturn(order);
        when(paymentMapper.updateStatus(eq(7L), eq((byte) 0), eq((byte) 1), eq("ALI001"), any()))
                .thenReturn(1);
        when(orderMapper.markPaid(eq(9L), eq((byte) 0), eq((byte) 1), any())).thenReturn(1);

        Map<String, String> params = successNotify("299.00");
        params.put("out_trade_no", "PAY001");
        service.handleAlipayNotify(params);

        verify(orderMapper).markPaid(eq(9L), eq((byte) 0), eq((byte) 1), any());
    }

    private Order order(OrderStatus status, String amount) {
        Order order = new Order();
        order.setId(9L);
        order.setOrderNo("EC001");
        order.setUserId(42L);
        order.setStatus(status.getCode());
        order.setTotalAmount(new BigDecimal(amount));
        return order;
    }

    private Payment payment(PaymentStatus status, String amount) {
        Payment payment = new Payment();
        payment.setId(7L);
        payment.setOrderId(9L);
        payment.setPaymentNo("PAY001");
        payment.setProvider("ALIPAY");
        payment.setAmount(new BigDecimal(amount));
        payment.setStatus(status.getCode());
        payment.setCreateTime(LocalDateTime.now());
        return payment;
    }

    private Map<String, String> successNotify(String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", "EC001");
        params.put("trade_no", "ALI001");
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("total_amount", amount);
        params.put("gmt_payment", "2026-08-26 12:30:00");
        return params;
    }
}
