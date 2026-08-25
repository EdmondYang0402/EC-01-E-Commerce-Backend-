package com.ec01.service;

import com.ec01.common.CategoryStatus;
import com.ec01.common.PageResult;
import com.ec01.dto.category.CategoryCreateDTO;
import com.ec01.dto.category.CategoryUpdateDTO;
import com.ec01.entity.Category;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.CategoryMapper;
import com.ec01.mapper.ProductMapper;
import com.ec01.service.impl.CategoryServiceImpl;
import com.ec01.vo.category.CategoryAdminVO;
import com.ec01.vo.category.CategoryProductCountVO;
import com.ec01.vo.category.CategoryVO;
import com.ec01.vo.product.ProductListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ProductMapper productMapper;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryMapper, productMapper);
    }

    @Test
    void getCategoryTreeUsesOneQueryAndGroupsTwoLevels() {
        when(categoryMapper.selectEnabledCategories()).thenReturn(List.of(
                category(1L, "电子数码", null, (byte) 1),
                category(101L, "手机配件", 1L, (byte) 1),
                category(2L, "电脑周边", null, (byte) 1),
                category(201L, "键盘", 2L, (byte) 1)));
        when(productMapper.countProductsByCategoryIds(List.of(101L, 201L))).thenReturn(List.of(
                count(101L, 12L),
                count(201L, 8L)));

        List<CategoryVO> tree = service.getCategoryTree();

        assertEquals(2, tree.size());
        assertEquals("手机配件", tree.getFirst().getChildren().getFirst().getName());
        assertEquals(12L, tree.getFirst().getProductCount());
        assertEquals(12L, tree.getFirst().getChildren().getFirst().getProductCount());
        assertEquals("键盘", tree.get(1).getChildren().getFirst().getName());
        assertEquals(8L, tree.get(1).getProductCount());
        verify(categoryMapper).selectEnabledCategories();
        verifyNoMoreInteractions(categoryMapper);
        verify(productMapper).countProductsByCategoryIds(List.of(101L, 201L));
    }

    @Test
    void getProductsByRootUsesEnabledChildrenAndDatabasePagination() {
        when(categoryMapper.selectById(1L))
                .thenReturn(category(1L, "电子数码", null, (byte) 1));
        when(categoryMapper.selectChildIds(1L)).thenReturn(List.of(101L, 102L));
        ProductListVO product = new ProductListVO();
        product.setId(10L);
        when(productMapper.countByCategoryIds(List.of(101L, 102L))).thenReturn(1L);
        when(productMapper.selectPageByCategoryIds(List.of(101L, 102L), 20L, 10))
                .thenReturn(List.of(product));

        PageResult<ProductListVO> result = service.getProductsByCategory(1L, 3, 10);

        assertEquals(1L, result.getTotal());
        assertEquals(10L, result.getRecords().getFirst().getId());
    }

    @Test
    void getProductsByChildRejectsDisabledParentBypass() {
        when(categoryMapper.selectById(101L))
                .thenReturn(category(101L, "手机配件", 1L, (byte) 1));
        when(categoryMapper.selectById(1L))
                .thenReturn(category(1L, "电子数码", null, (byte) 0));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getProductsByCategory(101L, 1, 10));

        assertEquals(409, exception.getCode());
        verifyNoInteractions(productMapper);
    }

    @Test
    void createCategoryRejectsThirdLevelParent() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setName("三级");
        dto.setParentId(101L);
        dto.setSortOrder(10);
        dto.setStatus(CategoryStatus.ENABLED);
        when(categoryMapper.selectById(101L))
                .thenReturn(category(101L, "手机配件", 1L, (byte) 1));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createCategory(dto));

        assertEquals(400, exception.getCode());
        verify(categoryMapper, never()).insert(any());
    }

    @Test
    void createUpdateStatusAndAdminQueryRemainAvailable() {
        Category root = category(1L, "电子数码", null, (byte) 1);
        root.setSortOrder(10);
        when(categoryMapper.insert(any())).thenReturn(1);

        CategoryCreateDTO create = new CategoryCreateDTO();
        create.setName(" 新分类 ");
        create.setSortOrder(20);
        create.setStatus(CategoryStatus.ENABLED);
        service.createCategory(create);

        ArgumentCaptor<Category> inserted = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(inserted.capture());
        assertEquals("新分类", inserted.getValue().getName());

        when(categoryMapper.selectById(1L)).thenReturn(root);
        when(categoryMapper.update(any())).thenReturn(1);
        CategoryUpdateDTO update = new CategoryUpdateDTO();
        update.setName(" 数码产品 ");
        update.setSortOrder(30);
        service.updateCategory(1L, update);
        assertEquals("数码产品", root.getName());

        when(categoryMapper.updateStatus(1L, (byte) 0)).thenReturn(1);
        service.updateCategoryStatus(1L, (byte) 0);

        when(categoryMapper.selectAllForAdmin()).thenReturn(List.of(root));
        List<CategoryAdminVO> admin = service.getAdminCategories();
        assertEquals(1, admin.size());
        assertEquals(CategoryStatus.ENABLED, admin.getFirst().getStatus());
    }

    @Test
    void rejectsInvalidStatusAndPagination() {
        when(categoryMapper.selectById(1L))
                .thenReturn(category(1L, "电子数码", null, (byte) 1));

        assertEquals(400, assertThrows(BusinessException.class,
                () -> service.updateCategoryStatus(1L, (byte) 2)).getCode());
        assertEquals(400, assertThrows(BusinessException.class,
                () -> service.getProductsByCategory(1L, 0, 10)).getCode());
    }

    private Category category(Long id, String name, Long parentId, byte status) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(10);
        category.setStatus(status);
        return category;
    }

    private CategoryProductCountVO count(Long categoryId, Long productCount) {
        CategoryProductCountVO count = new CategoryProductCountVO();
        count.setCategoryId(categoryId);
        count.setProductCount(productCount);
        return count;
    }
}
