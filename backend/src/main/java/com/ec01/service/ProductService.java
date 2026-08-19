package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.product.ProductQueryDTO;
import com.ec01.vo.product.ProductDetailVO;
import com.ec01.vo.product.ProductListVO;

public interface ProductService {
    PageResult<ProductListVO> getProductPage(ProductQueryDTO dto);

    ProductDetailVO getProductDetail(Long productId);
}
