package com.ec01.mapper;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CartItemMapperSqlTest {

    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(CartItemMapper.class);
    }

    @Test
    void cartQueryMapsAllCartItemVoFieldsAndFiltersByUser() {
        MappedStatement statement = configuration.getMappedStatement(
                CartItemMapper.class.getName() + ".selectCartByUserId");
        String sql = normalize(statement.getBoundSql(7L).getSql());

        assertTrue(sql.contains("c.id as cart_item_id"));
        assertTrue(sql.contains("c.sku_id"));
        assertTrue(sql.contains("c.quantity"));
        assertTrue(sql.contains("c.selected"));
        assertTrue(sql.contains("s.product_id"));
        assertTrue(sql.contains("s.spec_json"));
        assertTrue(sql.contains("s.price"));
        assertTrue(sql.contains("s.stock"));
        assertTrue(sql.contains("p.name as product_name"));
        assertTrue(sql.contains("p.cover_url"));
        assertTrue(sql.contains("where c.user_id = ?"));
    }

    @Test
    void ownershipQueriesUseBothItemAndUserId() {
        Map<String, Object> params = Map.of("id", 1L, "userId", 7L);
        String selectSql = normalize(configuration.getMappedStatement(
                CartItemMapper.class.getName() + ".selectByIdAndUserId")
                .getBoundSql(params).getSql());
        String deleteSql = normalize(configuration.getMappedStatement(
                CartItemMapper.class.getName() + ".deleteByIdAndUserId")
                .getBoundSql(params).getSql());

        Map<String, Object> updateParams = Map.of(
                "id", 1L,
                "userId", 7L,
                "quantity", 2,
                "selected", 1);
        String updateSql = normalize(configuration.getMappedStatement(
                CartItemMapper.class.getName() + ".update")
                .getBoundSql(updateParams).getSql());

        assertTrue(selectSql.contains("where id = ? and user_id = ?"));
        assertTrue(deleteSql.contains("where id = ? and user_id = ?"));
        assertTrue(updateSql.contains("where id = ? and user_id = ?"));
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
