package com.ec01.mapper;

import com.ec01.entity.Payment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface PaymentMapper {

    @Insert("""
            INSERT INTO payment (
                order_id, payment_no, provider, provider_trade_no, amount,
                status, paid_time, create_time, update_time
            ) VALUES (
                #{orderId}, #{paymentNo}, #{provider}, #{providerTradeNo}, #{amount},
                #{status}, #{paidTime}, #{createTime}, #{updateTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Select("""
            SELECT *
            FROM payment
            WHERE order_id = #{orderId}
              AND status = #{status}
            ORDER BY id DESC
            LIMIT 1
            FOR UPDATE
            """)
    Payment selectByOrderIdAndStatusForUpdate(
            @Param("orderId") Long orderId,
            @Param("status") Byte status
    );

    @Select("""
            SELECT *
            FROM payment
            WHERE order_id = #{orderId}
            ORDER BY id DESC
            LIMIT 1
            """)
    Payment selectLatestByOrderId(@Param("orderId") Long orderId);

    @Select("""
            SELECT *
            FROM payment
            WHERE order_id = #{orderId}
            ORDER BY id DESC
            LIMIT 1
            FOR UPDATE
            """)
    Payment selectLatestByOrderIdForUpdate(@Param("orderId") Long orderId);

    @Select("""
            SELECT *
            FROM payment
            WHERE payment_no = #{paymentNo}
            LIMIT 1
            FOR UPDATE
            """)
    Payment selectByPaymentNoForUpdate(@Param("paymentNo") String paymentNo);

    @Update("""
            UPDATE payment
            SET status = #{newStatus},
                provider_trade_no = #{providerTradeNo},
                paid_time = #{paidTime},
                update_time = NOW()
            WHERE id = #{paymentId}
              AND status = #{oldStatus}
            """)
    int updateStatus(
            @Param("paymentId") Long paymentId,
            @Param("oldStatus") Byte oldStatus,
            @Param("newStatus") Byte newStatus,
            @Param("providerTradeNo") String providerTradeNo,
            @Param("paidTime") LocalDateTime paidTime
    );
}
