package com.ec01.controller;

import com.ec01.common.PageResult;
import com.ec01.common.Result;
import com.ec01.dto.admin.product.ProductAdminQueryDTO;
import com.ec01.dto.admin.product.ProductCreateDTO;
import com.ec01.dto.admin.product.ProductStatusUpdateDTO;
import com.ec01.dto.admin.product.ProductUpdateDTO;
import com.ec01.dto.admin.product.SkuCreateDTO;
import com.ec01.dto.admin.product.SkuStatusUpdateDTO;
import com.ec01.dto.admin.product.SkuUpdateDTO;
import com.ec01.service.AdminProductService;
import com.ec01.vo.admin.product.AdminProductDetailVO;
import com.ec01.vo.admin.product.AdminProductListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService adminProductService;

    @GetMapping("/products")
    public Result<PageResult<AdminProductListVO>> getProductPage(
            @Valid @ModelAttribute ProductAdminQueryDTO dto) {
        return Result.success(adminProductService.getProductPage(dto));
    }

    @GetMapping("/products/{productId}")
    public Result<AdminProductDetailVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(adminProductService.getProductDetail(productId));
    }

    @PostMapping("/products")
    public Result<Long> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return Result.success(adminProductService.createProduct(dto));
    }

    @PutMapping("/products/{productId}")
    public Result<Void> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateDTO dto) {
        adminProductService.updateProduct(productId, dto);
        return Result.success(null);
    }

    @PatchMapping("/products/{productId}/status")
    public Result<Void> changeStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateDTO dto) {
        adminProductService.changeStatus(productId, dto);
        return Result.success(null);
    }

    @PostMapping("/products/{productId}/skus")
    public Result<Long> addSku(
            @PathVariable Long productId,
            @Valid @RequestBody SkuCreateDTO dto) {
        return Result.success(adminProductService.addSku(productId, dto));
    }

    @PutMapping("/skus/{skuId}")
    public Result<Void> updateSku(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuUpdateDTO dto) {
        adminProductService.updateSku(skuId, dto);
        return Result.success(null);
    }

    @PatchMapping("/skus/{skuId}/status")
    public Result<Void> changeSkuStatus(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuStatusUpdateDTO dto) {
        adminProductService.changeSkuStatus(skuId, dto);
        return Result.success(null);
    }
}
