package com.ec01.mapper;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminManagementMapperSqlTest {
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(ProductMapper.class);
        configuration.addMapper(OrderMapper.class);
        configuration.addMapper(OrderItemMapper.class);
        configuration.addMapper(UserMapper.class);
    }

    @Test
    void productPageAndCountShareAdminFilters() {
        Map<String, Object> page = Map.of(
                "keyword", "chair", "status", (byte) 1, "categoryId", 2L,
                "offset", 10L, "size", 10);
        Map<String, Object> count = Map.of(
                "keyword", "chair", "status", (byte) 1, "categoryId", 2L);

        String pageSql = sql(ProductMapper.class, "selectAdminPage", page);
        String countSql = sql(ProductMapper.class, "countAdminProducts", count);

        assertTrue(pageSql.contains("name like concat('%', ?, '%')"));
        assertTrue(pageSql.contains("status = ?"));
        assertTrue(pageSql.contains("category_id = ?"));
        assertTrue(pageSql.contains("limit ?, ?"));
        assertTrue(countSql.contains("status = ?"));
        assertTrue(countSql.contains("category_id = ?"));
    }

    @Test
    void orderPageAndCountShareAdminFilters() {
        Map<String, Object> page = Map.of(
                "orderNo", "EC", "status", (byte) 1, "userId", 42L,
                "offset", 0L, "size", 20);
        Map<String, Object> count = Map.of(
                "orderNo", "EC", "status", (byte) 1, "userId", 42L);

        String pageSql = sql(OrderMapper.class, "selectAdminPage", page);
        String countSql = sql(OrderMapper.class, "countAdminOrders", count);
        String detailSql = sql(OrderMapper.class, "selectByOrderNo", Map.of("orderNo", "EC"));

        assertTrue(pageSql.contains("order_no like concat('%', ?, '%')"));
        assertTrue(pageSql.contains("status = ?"));
        assertTrue(pageSql.contains("user_id = ?"));
        assertTrue(pageSql.contains("limit ?, ?"));
        assertTrue(countSql.contains("user_id = ?"));
        assertTrue(detailSql.contains("where order_no = ?"));
        assertFalse(detailSql.contains("user_id = ?"));
    }

    @Test
    void userPageFiltersAndDedicatedStatusUpdateAreLimited() {
        Map<String, Object> page = Map.of(
                "keyword", "alice", "status", 1, "offset", 0L, "size", 20);
        String pageSql = sql(UserMapper.class, "selectAdminPage", page);
        String statusSql = sql(UserMapper.class, "updateStatus", Map.of("id", 5L, "status", 0));

        assertTrue(pageSql.contains("username like concat('%', ?, '%')"));
        assertTrue(pageSql.contains("nickname like concat('%', ?, '%')"));
        assertTrue(pageSql.contains("phone like concat('%', ?, '%')"));
        assertTrue(pageSql.contains("status = ?"));
        assertTrue(pageSql.contains("limit ?, ?"));
        assertTrue(statusSql.contains("set status = ?"));
        assertFalse(statusSql.contains("password = ?"));
        assertFalse(statusSql.contains("nickname = ?"));
    }

    @Test
    void adminOrderItemsUseSnapshotColumnAliases() {
        String sql = sql(
                OrderItemMapper.class, "selectAdminByOrderId", Map.of("orderId", 7L));

        assertTrue(sql.contains("product_name as productname"));
        assertTrue(sql.contains("sku_spec as skuspec"));
        assertTrue(sql.contains("cover_url as coverurl"));
        assertTrue(sql.contains("where order_id = ?"));
    }

    private String sql(Class<?> mapper, String method, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(mapper.getName() + "." + method);
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
