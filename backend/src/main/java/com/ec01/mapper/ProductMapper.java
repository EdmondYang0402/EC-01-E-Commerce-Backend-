package com.ec01.mapper;

import com.ec01.entity.Product;
import com.ec01.vo.product.ProductListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    @Select("""
    <script>
    SELECT
        p.id,
        p.name,
        p.subtitle,
        p.cover_url,
        p.status,
        (
            SELECT MIN(s.price)
            FROM sku s
            WHERE s.product_id = p.id
              AND s.status = 1
        ) AS min_price
    FROM product p
    WHERE p.status = 1

    <if test="keyword != null and keyword != ''">
        AND p.name LIKE CONCAT('%', #{keyword}, '%')
    </if>

    <if test="categoryId != null">
        AND p.category_id = #{categoryId}
    </if>

    ORDER BY p.id DESC
    LIMIT #{offset}, #{pageSize}
    </script>
    """)
    List<ProductListVO> selectProductPage(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("offset") Long offset,
            @Param("pageSize") Integer pageSize
    );

    @Select("""
    <script>
    SELECT COUNT(*)
    FROM product
    WHERE status = 1

    <if test="keyword != null and keyword != ''">
        AND name LIKE CONCAT('%', #{keyword}, '%')
    </if>

    <if test="categoryId != null">
        AND category_id = #{categoryId}
    </if>
    </script>
    """)
    long countProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId
    );
}
