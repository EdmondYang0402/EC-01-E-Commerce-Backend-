package com.ec01.mapper;


import com.ec01.vo.order.OrderItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
