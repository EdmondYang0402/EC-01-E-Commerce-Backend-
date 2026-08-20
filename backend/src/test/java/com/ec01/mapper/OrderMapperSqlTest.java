package com.ec01.mapper;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderMapperSqlTest {

    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(OrderMapper.class);
        configuration.addMapper(OrderItemMapper.class);
    }

    @Test
    void orderPageAndCountAreLimitedToCurrentUser() {
        BoundSql pageSql = statement(OrderMapper.class, "selectPageByUserId")
                .getBoundSql(Map.of("userId", 42L, "offset", 10L, "size", 10));
        String page = normalize(pageSql.getSql());
        String count = normalize(statement(OrderMapper.class, "countByUserId")
                .getBoundSql(Map.of("userId", 42L)).getSql());

        assertTrue(page.contains("where user_id = ?"));
        assertTrue(page.contains("order by create_time desc"));
        assertTrue(page.contains("limit ?, ?"));
        assertTrue(count.contains("select count(*) from orders where user_id = ?"));
    }

    @Test
    void orderItemsMapEveryOrderItemVoField() {
        String sql = normalize(statement(OrderItemMapper.class, "selectByOrderId")
                .getBoundSql(Map.of("orderId", 9L)).getSql());

        assertTrue(sql.contains("product_id as productid"));
        assertTrue(sql.contains("sku_id as skuid"));
        assertTrue(sql.contains("product_name as productname"));
        assertTrue(sql.contains("sku_spec as skuspec"));
        assertTrue(sql.contains("cover_url as coverurl"));
        assertTrue(sql.contains("where order_id = ?"));
    }

    @Test
    void orderInsertPersistsReceiverSnapshot() {
        String sql = normalize(statement(OrderMapper.class, "insert")
                .getBoundSql(new com.ec01.entity.Order()).getSql());

        assertTrue(sql.contains("receiver_name"));
        assertTrue(sql.contains("receiver_phone"));
        assertTrue(sql.contains("receiver_address"));
    }

    private MappedStatement statement(Class<?> mapper, String method) {
        return configuration.getMappedStatement(mapper.getName() + "." + method);
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
