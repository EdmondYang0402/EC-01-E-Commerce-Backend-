package com.ec01.service.impl;

import com.ec01.common.PageResult;
import com.ec01.common.ProductStatus;
import com.ec01.dto.product.ProductQueryDTO;
import com.ec01.entity.Product;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.service.ProductService;
import com.ec01.vo.product.ProductDetailVO;
import com.ec01.vo.product.ProductListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    @Override
    public PageResult<ProductListVO> getProductPage(ProductQueryDTO dto) {
        if (dto == null || dto.getPage() == null || dto.getSize() == null
                || dto.getPage() < 1 || dto.getSize() < 1 || dto.getSize() > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }

        String keyword = normalizeKeyword(dto.getKeyword());
        long offset = (long) (dto.getPage() - 1) * dto.getSize();
        List<ProductListVO> records = productMapper.selectProductPage(
                keyword, dto.getCategoryId(), offset, dto.getSize());
        long total = productMapper.countProducts(keyword, dto.getCategoryId());

        return new PageResult<>(records, total);
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(400, "商品ID不合法");
        }

        Product product = productMapper.selectById(productId);

        if (product == null || product.getStatus() == null
                || product.getStatus() != ProductStatus.ON_SHELF.getCode()) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        ProductDetailVO productDetailVO = new ProductDetailVO();

        productDetailVO.setId(product.getId());
        productDetailVO.setName(product.getName());
        productDetailVO.setSubtitle(product.getSubtitle());
        productDetailVO.setDescription(product.getDescription());
        productDetailVO.setCategoryId(product.getCategoryId());
        productDetailVO.setCoverUrl(product.getCoverUrl());
        productDetailVO.setStatus(product.getStatus());

        productDetailVO.setSkus(
                skuMapper.selectByProductId(productId)
        );

        return productDetailVO;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
