package com.ec01.service.impl;

import com.ec01.entity.Category;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.CategoryMapper;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryValidationTest {
    @Mock private ProductMapper productMapper;
    @Mock private SkuMapper skuMapper;
    @Mock private CategoryService categoryService;
    @Mock private CategoryMapper categoryMapper;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productMapper, skuMapper, categoryService, categoryMapper);
    }

    @Test
    void acceptsEnabledChildOfEnabledRoot() {
        when(categoryMapper.selectById(101L)).thenReturn(category(101L, 1L, (byte) 1));
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, null, (byte) 1));

        assertDoesNotThrow(() -> service.validateProductCategory(101L));
    }

    @Test
    void rejectsRootDisabledChildAndDisabledParent() {
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, null, (byte) 1));
        assertEquals(400, assertThrows(BusinessException.class,
                () -> service.validateProductCategory(1L)).getCode());

        when(categoryMapper.selectById(101L)).thenReturn(category(101L, 1L, (byte) 0));
        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.validateProductCategory(101L)).getCode());

        when(categoryMapper.selectById(101L)).thenReturn(category(101L, 1L, (byte) 1));
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, null, (byte) 0));
        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.validateProductCategory(101L)).getCode());
    }

    private Category category(Long id, Long parentId, byte status) {
        Category category = new Category();
        category.setId(id);
        category.setParentId(parentId);
        category.setStatus(status);
        return category;
    }
}
