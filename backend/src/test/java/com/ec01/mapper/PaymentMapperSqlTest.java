package com.ec01.mapper;

import com.ec01.entity.Payment;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentMapperSqlTest {
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(PaymentMapper.class);
    }

    @Test
    void paymentInsertPersistsProviderAndDatabaseTrustedAmount() {
        String sql = normalize(statement("insert").getBoundSql(new Payment()).getSql());

        assertTrue(sql.contains("order_id"));
        assertTrue(sql.contains("payment_no"));
        assertTrue(sql.contains("provider_trade_no"));
        assertTrue(sql.contains("amount"));
    }

    @Test
    void paymentSuccessUpdateUsesPendingStatusCas() {
        BoundSql boundSql = statement("updateStatus").getBoundSql(Map.of(
                "paymentId", 7L,
                "oldStatus", (byte) 0,
                "newStatus", (byte) 1,
                "providerTradeNo", "ALI001",
                "paidTime", LocalDateTime.now()));
        String sql = normalize(boundSql.getSql());

        assertTrue(sql.contains("provider_trade_no = ?"));
        assertTrue(sql.contains("paid_time = ?"));
        assertTrue(sql.contains("where id = ? and status = ?"));
    }

    @Test
    void notificationLookupLocksPaymentRow() {
        String sql = normalize(statement("selectByPaymentNoForUpdate")
                .getBoundSql(Map.of("paymentNo", "PAY001")).getSql());

        assertTrue(sql.contains("where payment_no = ?"));
        assertTrue(sql.endsWith("for update"));
    }

    @Test
    void orderPaymentLookupLocksLatestPaymentRow() {
        String sql = normalize(statement("selectLatestByOrderIdForUpdate")
                .getBoundSql(Map.of("orderId", 9L)).getSql());

        assertTrue(sql.contains("where order_id = ?"));
        assertTrue(sql.contains("order by id desc"));
        assertTrue(sql.endsWith("for update"));
    }

    private MappedStatement statement(String method) {
        return configuration.getMappedStatement(PaymentMapper.class.getName() + "." + method);
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
