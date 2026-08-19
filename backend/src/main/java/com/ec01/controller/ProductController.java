package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.product.ProductQueryDTO;
import com.ec01.service.ProductService;
import com.ec01.vo.product.ProductDetailVO;
import com.ec01.vo.product.ProductListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductListVO>> getProductPage(
            @Valid @ModelAttribute ProductQueryDTO dto) {
        return Result.success(productService.getProductPage(dto));
    }

    @GetMapping("/{productId}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }
}
