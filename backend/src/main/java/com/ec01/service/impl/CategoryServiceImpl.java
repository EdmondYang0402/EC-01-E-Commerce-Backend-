package com.ec01.service.impl;

import com.ec01.common.CategoryStatus;
import com.ec01.common.PageResult;
import com.ec01.dto.category.CategoryCreateDTO;
import com.ec01.dto.category.CategoryUpdateDTO;
import com.ec01.entity.Category;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.CategoryMapper;
import com.ec01.mapper.ProductMapper;
import com.ec01.service.CategoryService;
import com.ec01.vo.category.CategoryAdminVO;
import com.ec01.vo.category.CategoryProductCountVO;
import com.ec01.vo.category.CategoryVO;
import com.ec01.vo.product.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> categories = categoryMapper.selectEnabledCategories();
        Map<Long, CategoryVO> roots = new LinkedHashMap<>();

        for (Category category : categories) {
            if (category.getParentId() == null) {
                CategoryVO root = toCategoryVO(category);
                root.setChildren(new ArrayList<>());
                roots.put(category.getId(), root);
            }
        }
        for (Category category : categories) {
            if (category.getParentId() != null) {
                CategoryVO parent = roots.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(toCategoryVO(category));
                }
            }
        }

        Map<Long, Long> productCounts = loadProductCounts(categories);
        for (CategoryVO root : roots.values()) {
            long rootCount = 0L;
            for (CategoryVO child : root.getChildren()) {
                long childCount = productCounts.getOrDefault(child.getId(), 0L);
                child.setProductCount(childCount);
                rootCount += childCount;
            }
            root.setProductCount(rootCount);
        }
        return new ArrayList<>(roots.values());
    }

    @Override
    public PageResult<ProductListVO> getProductsByCategory(
            Long categoryId,
            Integer page,
            Integer size) {

        // 1. 基础参数校验
        validateCategoryPage(categoryId, page, size);

        // 2. 查询分类
        Category category = categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        if (!isEnabled(category)) {
            throw new BusinessException(409, "分类不可用");
        }

        // 3. 计算分页偏移量
        long offset = (page - 1L) * size;

        // 4. 准备真正用于查询商品的分类 ID
        List<Long> categoryIds = new ArrayList<>();

        // 一级分类：查它下面所有二级分类
        if (category.getParentId() == null) {

            categoryIds.addAll(categoryMapper.selectChildIds(categoryId));

        } else {
            Category parent = requireCategory(category.getParentId());
            if (parent.getParentId() != null) {
                throw new BusinessException(409, "分类层级数据异常");
            }
            if (!isEnabled(parent)) {
                throw new BusinessException(409, "所属一级分类不可用");
            }
            categoryIds.add(categoryId);
        }

        // 5. 一级分类下面没有任何可用子分类
        if (categoryIds.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }

        // 6. 查询符合条件的商品总数
        long total =
                productMapper.countByCategoryIds(categoryIds);

        // 7. 查询当前页商品
        List<ProductListVO> productListVO =
                productMapper.selectPageByCategoryIds(
                        categoryIds,
                        offset,
                        size
                );

        // 8. 组装分页结果
        return new PageResult<>(productListVO, total);
    }


    @Override
    public void createCategory(CategoryCreateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()
                || dto.getSortOrder() == null || dto.getSortOrder() < 0 || dto.getSortOrder() > 9999
                || dto.getStatus() == null) {
            throw new BusinessException(400, "分类信息不合法");
        }

        if (dto.getParentId() != null) {
            Category parent = requireCategory(dto.getParentId());
            if (parent.getParentId() != null) {
                throw new BusinessException(400, "只允许创建两级分类，父分类必须是一级分类");
            }
        }

        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setParentId(dto.getParentId());
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus().getCode());
        if (categoryMapper.insert(category) <= 0) {
            throw new BusinessException(500, "分类创建失败");
        }
    }

    @Override
    public void updateCategory(Long categoryId, CategoryUpdateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()
                || dto.getSortOrder() == null || dto.getSortOrder() < 0 || dto.getSortOrder() > 9999) {
            throw new BusinessException(400, "分类信息不合法");
        }
        Category category = requireCategory(categoryId);
        category.setName(dto.getName().trim());
        category.setSortOrder(dto.getSortOrder());
        if (categoryMapper.update(category) <= 0) {
            throw new BusinessException(500, "分类更新失败");
        }
    }

    @Override
    public void updateCategoryStatus(Long categoryId, Byte status) {
        requireCategory(categoryId);
        if (status == null || (status != CategoryStatus.ENABLED.getCode()
                && status != CategoryStatus.DISABLED.getCode())) {
            throw new BusinessException(400, "分类状态不合法");
        }
        if (categoryMapper.updateStatus(categoryId, status) <= 0) {
            throw new BusinessException(500, "分类状态更新失败");
        }
    }

    @Override
    public List<CategoryAdminVO> getAdminCategories() {
        return categoryMapper.selectAllForAdmin().stream()
                .map(this::toAdminVO)
                .toList();
    }

    private Category requireCategory(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(400, "分类ID不合法");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    private void validateCategoryPage(Long categoryId, Integer page, Integer size) {
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(400, "分类ID不合法");
        }
        if (page == null || size == null || page < 1 || size < 1 || size > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setProductCount(0L);
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private Map<Long, Long> loadProductCounts(List<Category> categories) {
        List<Long> childIds = categories.stream()
                .filter(category -> category.getParentId() != null)
                .map(Category::getId)
                .toList();
        if (childIds.isEmpty()) {
            return Map.of();
        }
        return productMapper.countProductsByCategoryIds(childIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        CategoryProductCountVO::getCategoryId,
                        CategoryProductCountVO::getProductCount));
    }

    private boolean isEnabled(Category category) {
        return category.getStatus() != null
                && category.getStatus() == CategoryStatus.ENABLED.getCode();
    }

    private CategoryAdminVO toAdminVO(Category category) {
        CategoryAdminVO vo = new CategoryAdminVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(toCategoryStatus(category.getStatus()));
        vo.setCreateTime(category.getCreateTime());
        vo.setUpdateTime(category.getUpdateTime());
        return vo;
    }

    private CategoryStatus toCategoryStatus(Byte code) {
        for (CategoryStatus status : CategoryStatus.values()) {
            if (code != null && status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(500, "分类状态数据异常");
    }
}
