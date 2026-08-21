package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.category.CategoryCreateDTO;
import com.ec01.dto.category.CategoryUpdateDTO;
import com.ec01.vo.category.CategoryAdminVO;
import com.ec01.vo.category.CategoryVO;
import com.ec01.vo.product.ProductListVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> getCategoryTree();

    PageResult<ProductListVO> getProductsByCategory(
            Long categoryId,
            Integer page,
            Integer size
    );

    void createCategory(CategoryCreateDTO dto);

    void updateCategory(Long categoryId, CategoryUpdateDTO dto);

    void updateCategoryStatus(Long categoryId, Byte status);

    List<CategoryAdminVO> getAdminCategories();
}
