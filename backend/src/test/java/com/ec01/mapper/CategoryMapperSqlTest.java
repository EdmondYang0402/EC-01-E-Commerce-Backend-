package com.ec01.mapper;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryMapperSqlTest {
    private Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(CategoryMapper.class);
    }

    @Test
    void publicTreeQueryLoadsEnabledTwoLevelCategoriesOnce() {
        String sql = sql("selectEnabledCategories", null);

        assertTrue(sql.contains("from category c left join category parent"));
        assertTrue(sql.contains("c.status = 1"));
        assertTrue(sql.contains("parent.parent_id is null"));
        assertTrue(sql.contains("parent.status = 1"));
        assertTrue(sql.contains("c.sort_order"));
    }

    @Test
    void writesUseAuthoritativeSortOrderColumn() {
        String insert = sql("insert", new com.ec01.entity.Category());
        String update = sql("update", new com.ec01.entity.Category());

        assertTrue(insert.contains("sort_order"));
        assertTrue(update.contains("sort_order = ?"));
    }

    private String sql(String method, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(
                CategoryMapper.class.getName() + "." + method);
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ").trim().toLowerCase();
    }
}
