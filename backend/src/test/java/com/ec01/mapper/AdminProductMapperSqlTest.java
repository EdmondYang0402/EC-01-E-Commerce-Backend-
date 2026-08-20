package com.ec01.mapper;

import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminProductMapperSqlTest {
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(ProductMapper.class);
        configuration.addMapper(SkuMapper.class);
    }

    @Test
    void productBasicUpdateCannotChangeStatus() {
        String sql = sql(ProductMapper.class, "update", new Product());

        assertTrue(sql.contains("name = ?"));
        assertTrue(sql.contains("category_id = ?"));
        assertTrue(sql.contains("cover_url = ?"));
        assertFalse(sql.contains("status = ?"));
        assertTrue(sql.contains("where id = ?"));
    }

    @Test
    void productStatusUsesDedicatedUpdate() {
        String sql = sql(ProductMapper.class, "updateStatus", Map.of("id", 1L, "status", (byte) 1));

        assertTrue(sql.contains("set status = ?"));
        assertFalse(sql.contains("name = ?"));
        assertTrue(sql.contains("where id = ?"));
    }

    @Test
    void skuBasicUpdateAndStatusUpdateAreSeparated() {
        String update = sql(SkuMapper.class, "update", new Sku());
        String status = sql(SkuMapper.class, "updateStatus", Map.of("id", 2L, "status", (byte) 0));

        assertTrue(update.contains("spec_json = ?"));
        assertTrue(update.contains("price = ?"));
        assertTrue(update.contains("stock = ?"));
        assertFalse(update.contains("status = ?"));
        assertFalse(update.contains("sku_code = ?"));
        assertTrue(status.contains("set status = ?"));
    }

    @Test
    void adminSkuListIncludesAllStatusesForProduct() {
        String sql = sql(SkuMapper.class, "selectAllByProductId", Map.of("productId", 1L));

        assertTrue(sql.contains("where product_id = ?"));
        assertFalse(sql.contains("status = 1"));
        assertTrue(sql.contains("order by id"));
    }

    private String sql(Class<?> mapper, String method, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(mapper.getName() + "." + method);
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
