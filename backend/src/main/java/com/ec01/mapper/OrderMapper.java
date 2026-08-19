package com.ec01.mapper;

import com.ec01.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 创建订单
     */
    @Insert("""
        INSERT INTO orders (
            user_id,
            order_no,
            total_amount,
            status,
            create_time,
            update_time
        )
        VALUES (
            #{userId},
            #{orderNo},
            #{totalAmount},
            #{status},
            #{createTime},
            #{updateTime}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);


    /**
     * 分页查询当前用户的订单
     */
    @Select("""
        SELECT *
        FROM orders
        WHERE user_id = #{userId}
        ORDER BY create_time DESC
        LIMIT #{offset}, #{size}
        """)
    List<Order> selectPageByUserId(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("size") Integer size
    );


    /**
     * 查询当前用户订单总数
     */
    @Select("""
        SELECT COUNT(*)
        FROM orders
        WHERE user_id = #{userId}
        """)
    long countByUserId(@Param("userId") Long userId);


    /**
     * 根据订单号查询当前用户的一笔订单
     *
     * userId 一起作为查询条件，
     * 避免用户通过猜测 orderNo 查询其他用户订单。
     */
    @Select("""
        SELECT *
        FROM orders
        WHERE order_no = #{orderNo}
          AND user_id = #{userId}
        LIMIT 1
        """)
    Order selectByOrderNoAndUserId(
            @Param("orderNo") String orderNo,
            @Param("userId") Long userId
    );
}
