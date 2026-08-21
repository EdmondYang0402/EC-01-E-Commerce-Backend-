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
import com.ec01.vo.category.CategoryVO;
import com.ec01.vo.product.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {
        // TODO 用户练习 1：查询可用一级分类及其可用二级分类，并组装 CategoryVO.children。
        // 已准备 CategoryMapper.selectRootCategories() 与 selectByParentId(parentId)。
        return List.of();
    }

    @Override
    public PageResult<ProductListVO> getProductsByCategory(
            Long categoryId,
            Integer page,
            Integer size) {
        validateCategoryPage(categoryId, page, size);

        // TODO 用户练习 2：判断 categoryId 是一级还是二级分类，并完成对应分页编排。
        // 一级分类可配合 selectChildIds() 与 ProductMapper 的 List 分类查询；
        // 二级分类可使用 ProductMapper 的单分类查询。还需拒绝禁用/不可见分类。
        return new PageResult<>(List.of(), 0L);
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
