package com.ec01.mapper;

import com.ec01.entity.Sku;
import com.ec01.vo.product.SkuVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SkuMapper {
    @Insert("""
            INSERT INTO sku (
                product_id, sku_code, spec_json, price, stock, status
            ) VALUES (
                #{productId}, #{skuCode}, #{specJson}, #{price}, #{stock}, #{status}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Sku sku);

    @Select("SELECT * FROM sku WHERE sku_code = #{skuCode}")
    Sku selectBySkuCode(@Param("skuCode") String skuCode);

    @Select("""
    SELECT
        id,
        product_id,
        sku_code,
        spec_json,
        price,
        stock,
        status
    FROM sku
    WHERE product_id = #{productId}
      AND status = 1
""")
    List<SkuVO> selectByProductId(Long productId);

    @Select("SELECT * FROM sku WHERE id = #{id}")
    Sku selectById(@Param("id") Long id);

    @Select("""
            SELECT *
            FROM sku
            WHERE product_id = #{productId}
            ORDER BY id
            """)
    List<Sku> selectAllByProductId(@Param("productId") Long productId);

    @Update("""
            UPDATE sku
            SET spec_json = #{specJson},
                price = #{price},
                stock = #{stock},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int update(Sku sku);

    @Update("""
            UPDATE sku
            SET status = #{status},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") byte status);

    @Update("""
    UPDATE sku
    SET stock = stock - #{quantity}
    WHERE id = #{skuId}
      AND stock >= #{quantity}
""")
    int deductStock(
            @Param("skuId") Long skuId,
            @Param("quantity") Integer quantity
    );

    @Select("""
    SELECT *
    FROM sku
    WHERE product_id = #{productId}
      AND spec_json = #{specJson}
    LIMIT 1
""")
    Sku selectByProductIdAndSpecJson(
            @Param("productId") Long productId,
            @Param("specJson") String specJson
    );
}
