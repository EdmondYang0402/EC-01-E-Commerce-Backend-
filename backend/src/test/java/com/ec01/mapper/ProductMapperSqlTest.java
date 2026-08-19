package com.ec01.mapper;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductMapperSqlTest {

    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(ProductMapper.class);
        configuration.addMapper(SkuMapper.class);
    }

    @Test
    void productPageSqlContainsActiveProductFiltersAndMinimumActiveSkuPrice() {
        MappedStatement statement = configuration.getMappedStatement(
                ProductMapper.class.getName() + ".selectProductPage");
        BoundSql boundSql = statement.getBoundSql(Map.of(
                "keyword", "chair",
                "categoryId", 2L,
                "offset", 0L,
                "pageSize", 20));
        String sql = normalize(boundSql.getSql());

        assertTrue(sql.contains("where p.status = 1"));
        assertTrue(sql.contains("min(s.price)"));
        assertTrue(sql.contains("s.status = 1"));
        assertTrue(sql.contains("p.name like concat"));
        assertTrue(sql.contains("p.category_id = ?"));
        assertTrue(sql.contains("limit ?, ?"));
    }

    @Test
    void optionalProductFiltersAreOmittedWhenAbsent() {
        MappedStatement statement = configuration.getMappedStatement(
                ProductMapper.class.getName() + ".selectProductPage");
        BoundSql boundSql = statement.getBoundSql(Map.of(
                "offset", 0L,
                "pageSize", 20));
        String sql = normalize(boundSql.getSql());

        assertFalse(sql.contains("p.name like concat"));
        assertFalse(sql.contains("p.category_id = ?"));
    }

    @Test
    void skuDetailSqlOnlyReturnsActiveSkusAndMapsStatus() {
        MappedStatement statement = configuration.getMappedStatement(
                SkuMapper.class.getName() + ".selectByProductId");
        String sql = normalize(statement.getBoundSql(1L).getSql());

        assertTrue(sql.contains("stock, status"));
        assertTrue(sql.contains("where product_id = ?"));
        assertTrue(sql.contains("and status = 1"));
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
