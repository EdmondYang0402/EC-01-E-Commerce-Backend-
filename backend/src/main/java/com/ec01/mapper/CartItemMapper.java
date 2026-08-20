package com.ec01.mapper;

import com.ec01.entity.CartItem;
import com.ec01.vo.cart.CartItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {
    @Select("""
    <script>
    SELECT *
    FROM cart_item
    WHERE user_id = #{userId}
      AND id IN
      <foreach collection="cartItemIds"
               item="cartItemId"
               open="("
               separator=","
               close=")">
          #{cartItemId}
      </foreach>
    </script>
""")
    List<CartItem> selectByUserIdAndCartItemIds(
            @Param("userId") Long userId,
            @Param("cartItemIds") List<Long> cartItemIds
    );

    @Select("""
        SELECT *
        FROM cart_item
        WHERE user_id = #{userId}
          AND sku_id = #{skuId}
    """)
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId,
                                    @Param("skuId") Long skuId);

    @Select("""
    SELECT *
    FROM cart_item
    WHERE id = #{id}
      AND user_id = #{userId}
""")
    CartItem selectByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );


    @Insert("""
        INSERT INTO cart_item (
            user_id,
            sku_id,
            quantity,
            selected
        )
        VALUES (
            #{userId},
            #{skuId},
            #{quantity},
            #{selected}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    @Update("""
        UPDATE cart_item
        SET quantity = #{quantity},
            selected = #{selected},
            update_time = NOW()
        WHERE id = #{id}
          AND user_id = #{userId}
    """)
    int update(CartItem cartItem);

    @Select("""
    SELECT
        c.id AS cart_item_id,
        c.sku_id,
        c.quantity,
        c.selected,

        s.product_id,
        s.spec_json,
        s.price,
        s.stock,

        p.name AS product_name,
        p.cover_url

    FROM cart_item c

    JOIN sku s
        ON s.id = c.sku_id

    JOIN product p
        ON p.id = s.product_id

    WHERE c.user_id = #{userId}

    ORDER BY c.id DESC
""")
    List<CartItemVO> selectCartByUserId(Long userId);

    @Delete("""
    DELETE FROM cart_item
    WHERE id = #{id}
      AND user_id = #{userId}
""")
    int deleteByIdAndUserId(@Param("id") Long id,
                            @Param("userId") Long userId);
}
