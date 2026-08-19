package com.ec01.mapper;

import com.ec01.entity.Sku;
import com.ec01.vo.product.SkuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkuMapper {
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
}
