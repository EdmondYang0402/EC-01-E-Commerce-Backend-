package com.ec01.mapper;

import com.ec01.entity.Product;
import com.ec01.vo.product.ProductListVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {
    @Insert("""
            INSERT INTO product (
                name, subtitle, description, category_id, cover_url, status
            ) VALUES (
                #{name}, #{subtitle}, #{description}, #{categoryId}, #{coverUrl}, #{status}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    @Update("""
            UPDATE product
            SET name = #{name},
                subtitle = #{subtitle},
                description = #{description},
                category_id = #{categoryId},
                cover_url = #{coverUrl},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int update(Product product);

    @Update("""
            UPDATE product
            SET status = #{status},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") byte status);

    @Select("""
            <script>
            SELECT *
            FROM product
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
                AND name LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="categoryId != null">
                AND category_id = #{categoryId}
            </if>
            ORDER BY create_time DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Product> selectAdminPage(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            @Param("categoryId") Long categoryId,
            @Param("offset") long offset,
            @Param("size") Integer size
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM product
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
                AND name LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
            <if test="categoryId != null">
                AND category_id = #{categoryId}
            </if>
            </script>
            """)
    long countAdminProducts(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            @Param("categoryId") Long categoryId
    );

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
