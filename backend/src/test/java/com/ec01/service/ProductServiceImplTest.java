package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.dto.product.ProductQueryDTO;
import com.ec01.entity.Product;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.service.impl.ProductServiceImpl;
import com.ec01.vo.product.ProductDetailVO;
import com.ec01.vo.product.ProductListVO;
import com.ec01.vo.product.SkuVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private SkuMapper skuMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productMapper, skuMapper);
    }

    @Test
    void getProductPageUsesDefaultsAndReturnsMinimumSkuPrice() {
        ProductQueryDTO dto = new ProductQueryDTO();
        ProductListVO product = productList(1L, "Chair", "499.00");
        when(productMapper.selectProductPage(null, null, 0L, 20))
                .thenReturn(List.of(product));
        when(productMapper.countProducts(null, null)).thenReturn(1L);

        PageResult<ProductListVO> result = productService.getProductPage(dto);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(new BigDecimal("499.00"), result.getRecords().getFirst().getMinPrice());
    }

    @Test
    void getProductPagePassesNormalizedFiltersAndOffset() {
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setPage(3);
        dto.setSize(10);
        dto.setKeyword("  chair  ");
        dto.setCategoryId(2L);
        when(productMapper.selectProductPage("chair", 2L, 20L, 10)).thenReturn(List.of());
        when(productMapper.countProducts("chair", 2L)).thenReturn(0L);

        PageResult<ProductListVO> result = productService.getProductPage(dto);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void getProductPageRejectsInvalidPagination() {
        ProductQueryDTO dto = new ProductQueryDTO();
        dto.setPage(0);
        dto.setSize(101);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.getProductPage(dto));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(productMapper, skuMapper);
    }

    @Test
    void getProductDetailReturnsActiveProductAndActiveSkus() {
        Product product = product(1L, (byte) 1);
        SkuVO sku = new SkuVO();
        sku.setId(10L);
        sku.setSkuCode("CHAIR-BLACK");
        sku.setPrice(new BigDecimal("499.00"));
        sku.setStock(8);
        sku.setStatus((byte) 1);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(skuMapper.selectByProductId(1L)).thenReturn(List.of(sku));

        ProductDetailVO result = productService.getProductDetail(1L);

        assertEquals(1L, result.getId());
        assertEquals("Chair", result.getName());
        assertEquals((byte) 1, result.getStatus());
        assertEquals(1, result.getSkus().size());
        assertEquals("CHAIR-BLACK", result.getSkus().getFirst().getSkuCode());
        assertEquals((byte) 1, result.getSkus().getFirst().getStatus());
    }

    @Test
    void getProductDetailRejectsMissingOrOffShelfProduct() {
        when(productMapper.selectById(1L)).thenReturn(null);
        BusinessException missing = assertThrows(BusinessException.class,
                () -> productService.getProductDetail(1L));
        assertEquals(404, missing.getCode());

        when(productMapper.selectById(2L)).thenReturn(product(2L, (byte) 0));
        BusinessException offShelf = assertThrows(BusinessException.class,
                () -> productService.getProductDetail(2L));
        assertEquals(404, offShelf.getCode());
        verifyNoInteractions(skuMapper);
    }

    @Test
    void getProductDetailRejectsInvalidId() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.getProductDetail(0L));

        assertEquals(400, exception.getCode());
        verifyNoInteractions(productMapper, skuMapper);
    }

    private ProductListVO productList(Long id, String name, String minPrice) {
        ProductListVO product = new ProductListVO();
        product.setId(id);
        product.setName(name);
        product.setMinPrice(new BigDecimal(minPrice));
        product.setStatus((byte) 1);
        return product;
    }

    private Product product(Long id, byte status) {
        Product product = new Product();
        product.setId(id);
        product.setName("Chair");
        product.setSubtitle("Black oak");
        product.setDescription("Lounge chair");
        product.setCategoryId(2L);
        product.setCoverUrl("https://example.com/chair.png");
        product.setStatus(status);
        return product;
    }
}
