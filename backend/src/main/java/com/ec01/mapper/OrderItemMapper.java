package com.ec01.mapper;


import com.ec01.entity.OrderItem;
import com.ec01.vo.order.OrderItemVO;
import com.ec01.vo.admin.order.AdminOrderItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("""
        SELECT
            id,
            order_id AS orderId,
            product_id AS productId,
            sku_id AS skuId,
            product_name AS productName,
            sku_spec AS skuSpec,
            cover_url AS coverUrl,
            price,
            quantity,
            subtotal,
            create_time AS createTime
        FROM order_item
        WHERE order_id = #{orderId}
        ORDER BY id
        """)
    List<OrderItemVO> selectByOrderId(@Param("orderId") Long orderId);

    @Select("""
        SELECT
            id,
            product_id AS productId,
            sku_id AS skuId,
            product_name AS productName,
            sku_spec AS skuSpec,
            cover_url AS coverUrl,
            price,
            quantity,
            subtotal
        FROM order_item
        WHERE order_id = #{orderId}
        ORDER BY id
        """)
    List<AdminOrderItemVO> selectAdminByOrderId(@Param("orderId") Long orderId);

    @Insert("""
    INSERT INTO order_item (
        order_id,
        product_id,
        sku_id,
        product_name,
        sku_spec,
        cover_url,
        price,
        quantity,
        subtotal,
        create_time
    )
    VALUES (
        #{orderId},
        #{productId},
        #{skuId},
        #{productName},
        #{skuSpec},
        #{coverUrl},
        #{price},
        #{quantity},
        #{subtotal},
        #{createTime}
    )
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);
}
