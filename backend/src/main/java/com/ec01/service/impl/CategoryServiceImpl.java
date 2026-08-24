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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CategoryVO> getCategoryTree() {

        // 1. 查询所有可用一级分类
        List<Category> categoryList =
                categoryMapper.selectRootCategories();

        List<CategoryVO> categoryListVo = new ArrayList<>();

        // 2. 遍历一级分类
        for (Category category : categoryList) {

            CategoryVO categoryVO = new CategoryVO();

            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());

            // 3. 查询当前一级分类下面的二级分类
            List<Category> children =
                    categoryMapper.selectByParentId(category.getId());

            List<CategoryVO> childVOList = new ArrayList<>();

            // 4. 二级分类 Category -> CategoryVO
            for (Category child : children) {

                CategoryVO childVO = new CategoryVO();

                childVO.setId(child.getId());
                childVO.setName(child.getName());

                childVOList.add(childVO);
            }

            // 5. 把二级分类挂到一级分类下面
            categoryVO.setChildren(childVOList);

            // 6. 一级分类加入最终结果
            categoryListVo.add(categoryVO);
        }

        return categoryListVo;
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
            throw new BusinessException("分类不存在");
        }

        if (category.getStatus() != 1) {
            throw new BusinessException("分类不可用");
        }

        // 3. 计算分页偏移量
        long offset = (page - 1L) * size;

        // 4. 准备真正用于查询商品的分类 ID
        List<Long> categoryIds = new ArrayList<>();

        // 一级分类：查它下面所有二级分类
        if (category.getParentId() == null) {

            List<Category> children =
                    categoryMapper.selectByParentId(categoryId);

            for (Category child : children) {

                // 只加入可用的二级分类
                if (child.getStatus() == 1) {
                    categoryIds.add(child.getId());
                }
            }

        } else {

            // 二级分类：直接查当前分类
            categoryIds.add(categoryId);
        }

        // 5. 一级分类下面没有任何可用子分类
        if (categoryIds.isEmpty()) {
            PageResult<ProductListVO> emptyResult = new PageResult<>();
            emptyResult.setTotal(0L);
            emptyResult.setRecords(new ArrayList<>());
            return emptyResult;
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
        PageResult<ProductListVO> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setRecords(productListVO);

        return pageResult;
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
    private void validateProductCategory(Long categoryId) {

        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException("分类ID不合法");
        }

        Category category = categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        if (category.getParentId() == null) {
            throw new BusinessException("商品只能绑定二级分类");
        }

        if (category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("分类已禁用");
        }

        Category parent = categoryMapper.selectById(category.getParentId());

        if (parent == null) {
            throw new BusinessException("所属一级分类不存在");
        }

        if (parent.getStatus() == null || parent.getStatus() != 1) {
            throw new BusinessException("所属一级分类已禁用");
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
