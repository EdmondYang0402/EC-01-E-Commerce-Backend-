package com.ec01.controller;

import com.ec01.common.Result;
import com.ec01.dto.category.CategoryCreateDTO;
import com.ec01.dto.category.CategoryStatusUpdateDTO;
import com.ec01.dto.category.CategoryUpdateDTO;
import com.ec01.service.CategoryService;
import com.ec01.vo.category.CategoryAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryAdminVO>> getAdminCategories() {
        return Result.success(categoryService.getAdminCategories());
    }

    @PostMapping
    public Result<Void> createCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        categoryService.createCategory(dto);
        return Result.success(null);
    }

    @PutMapping("/{categoryId}")
    public Result<Void> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateDTO dto) {
        categoryService.updateCategory(categoryId, dto);
        return Result.success(null);
    }

    @PatchMapping("/{categoryId}/status")
    public Result<Void> updateCategoryStatus(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryStatusUpdateDTO dto) {
        categoryService.updateCategoryStatus(categoryId, dto.getStatus().getCode());
        return Result.success(null);
    }
}
