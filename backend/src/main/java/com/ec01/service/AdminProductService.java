package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.admin.product.ProductAdminQueryDTO;
import com.ec01.dto.admin.product.ProductCreateDTO;
import com.ec01.dto.admin.product.ProductStatusUpdateDTO;
import com.ec01.dto.admin.product.ProductUpdateDTO;
import com.ec01.dto.admin.product.SkuCreateDTO;
import com.ec01.dto.admin.product.SkuStatusUpdateDTO;
import com.ec01.dto.admin.product.SkuUpdateDTO;
import com.ec01.vo.admin.product.AdminProductDetailVO;
import com.ec01.vo.admin.product.AdminProductListVO;

public interface AdminProductService {
    PageResult<AdminProductListVO> getProductPage(ProductAdminQueryDTO dto);

    AdminProductDetailVO getProductDetail(Long productId);

    Long createProduct(ProductCreateDTO dto);

    void updateProduct(Long productId, ProductUpdateDTO dto);

    void changeStatus(Long productId, ProductStatusUpdateDTO dto);

    Long addSku(Long productId, SkuCreateDTO dto);

    void updateSku(Long skuId, SkuUpdateDTO dto);

    void changeSkuStatus(Long skuId, SkuStatusUpdateDTO dto);
}
