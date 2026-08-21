package com.ec01.service;

import com.ec01.common.PageResult;
import com.ec01.common.ProductStatus;
import com.ec01.common.SkuStatus;
import com.ec01.dto.admin.product.ProductAdminQueryDTO;
import com.ec01.dto.admin.product.ProductUpdateDTO;
import com.ec01.dto.admin.product.ProductStatusUpdateDTO;
import com.ec01.dto.admin.product.SkuUpdateDTO;
import com.ec01.dto.admin.product.SkuStatusUpdateDTO;
import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.service.impl.AdminProductServiceImpl;
import com.ec01.service.impl.ProductServiceImpl;
import com.ec01.vo.admin.product.AdminProductDetailVO;
import com.ec01.vo.admin.product.AdminProductListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminProductServiceImplTest {
    private ProductMapper productMapper;
    private SkuMapper skuMapper;
    private ProductServiceImpl productService;
    private AdminProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productMapper = mock(ProductMapper.class);
        skuMapper = mock(SkuMapper.class);
        productService = mock(ProductServiceImpl.class);
        service = new AdminProductServiceImpl(productMapper, skuMapper, productService);
    }

    @Test
    void productPageUsesBackendPaginationAndFilters() {
        ProductAdminQueryDTO dto = new ProductAdminQueryDTO();
        dto.setPage(3);
        dto.setSize(10);
        dto.setKeyword(" chair ");
        dto.setStatus(ProductStatus.ON_SHELF);
        dto.setCategoryId(2L);
        Product product = product(9L, (byte) 1);
        when(productMapper.selectAdminPage("chair", (byte) 1, 2L, 20L, 10))
                .thenReturn(List.of(product));
        when(productMapper.countAdminProducts("chair", (byte) 1, 2L)).thenReturn(21L);

        PageResult<AdminProductListVO> result = service.getProductPage(dto);

        assertEquals(21L, result.getTotal());
        assertEquals(ProductStatus.ON_SHELF, result.getRecords().getFirst().getStatus());
    }

    @Test
    void productDetailReturnsAllSkusIncludingDisabled() {
        Product product = product(9L, (byte) 0);
        Sku enabled = sku(11L, 9L, (byte) 1, "{\"color\":\"black\"}");
        Sku disabled = sku(12L, 9L, (byte) 0, "{\"color\":\"white\"}");
        when(productMapper.selectById(9L)).thenReturn(product);
        when(skuMapper.selectAllByProductId(9L)).thenReturn(List.of(enabled, disabled));

        AdminProductDetailVO detail = service.getProductDetail(9L);

        assertEquals(ProductStatus.OFF_SHELF, detail.getStatus());
        assertEquals(2, detail.getSkus().size());
        assertEquals(SkuStatus.DISABLED, detail.getSkus().get(1).getStatus());
    }

    @Test
    void updatesOnlyProductBaseFields() {
        when(productMapper.selectById(9L)).thenReturn(product(9L, (byte) 1));
        when(productMapper.update(argThat(product ->
                product.getId() == 9L
                        && "Updated".equals(product.getName())
                        && product.getStatus() == 1))).thenReturn(1);
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setName(" Updated ");
        dto.setCategoryId(3L);

        service.updateProduct(9L, dto);

        verify(productMapper).update(argThat(product ->
                "Updated".equals(product.getName()) && product.getCategoryId() == 3L));
    }

    @Test
    void rejectsDuplicateSkuSpecAndAllowsKeepingCurrentSpec() {
        Sku current = sku(11L, 9L, (byte) 1, "old");
        Sku duplicate = sku(12L, 9L, (byte) 1, "new");
        when(skuMapper.selectById(11L)).thenReturn(current);
        when(skuMapper.selectByProductIdAndSpecJson(9L, "new"))
                .thenReturn(duplicate, current);
        when(skuMapper.update(current)).thenReturn(1);
        SkuUpdateDTO dto = new SkuUpdateDTO();
        dto.setSpecJson("new");
        dto.setPrice(new BigDecimal("10.00"));
        dto.setStock(5);

        BusinessException conflict = assertThrows(
                BusinessException.class, () -> service.updateSku(11L, dto));
        assertEquals(409, conflict.getCode());
        verify(skuMapper, never()).update(current);

        service.updateSku(11L, dto);
        verify(skuMapper).update(argThat(sku ->
                sku.getId() == 11L && "new".equals(sku.getSpecJson())
                        && sku.getStock() == 5));
    }

    @Test
    void missingProductReturnsNotFound() {
        when(productMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.getProductDetail(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    void productRequiresEnabledSkuBeforeGoingOnShelf() {
        Product product = product(9L, (byte) 0);
        when(productMapper.selectById(9L)).thenReturn(product);
        when(skuMapper.selectAllByProductId(9L)).thenReturn(List.of(
                sku(11L, 9L, (byte) 0, "disabled")));
        ProductStatusUpdateDTO dto = new ProductStatusUpdateDTO();
        dto.setStatus(ProductStatus.ON_SHELF);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.changeStatus(9L, dto));

        assertEquals(409, exception.getCode());
        verify(productMapper, never()).updateStatus(9L, (byte) 1);
    }

    @Test
    void productWithEnabledSkuCanGoOnShelf() {
        Product product = product(9L, (byte) 0);
        when(productMapper.selectById(9L)).thenReturn(product);
        when(skuMapper.selectAllByProductId(9L)).thenReturn(List.of(
                sku(11L, 9L, (byte) 1, "enabled")));
        when(productMapper.updateStatus(9L, (byte) 1)).thenReturn(1);
        ProductStatusUpdateDTO dto = new ProductStatusUpdateDTO();
        dto.setStatus(ProductStatus.ON_SHELF);

        service.changeStatus(9L, dto);

        verify(productMapper).updateStatus(9L, (byte) 1);
    }

    @Test
    void cannotDisableLastEnabledSkuOfOnShelfProduct() {
        Product product = product(9L, (byte) 1);
        Sku sku = sku(11L, 9L, (byte) 1, "enabled");
        when(skuMapper.selectById(11L)).thenReturn(sku);
        when(productMapper.selectById(9L)).thenReturn(product);
        when(skuMapper.selectAllByProductId(9L)).thenReturn(List.of(sku));
        SkuStatusUpdateDTO dto = new SkuStatusUpdateDTO();
        dto.setStatus(SkuStatus.DISABLED);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.changeSkuStatus(11L, dto));

        assertEquals(409, exception.getCode());
        verify(skuMapper, never()).updateStatus(11L, (byte) 0);
    }

    @Test
    void skuUpdateRejectsZeroPriceAtServiceBoundary() {
        SkuUpdateDTO dto = new SkuUpdateDTO();
        dto.setSpecJson("valid");
        dto.setPrice(BigDecimal.ZERO);
        dto.setStock(0);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.updateSku(11L, dto));

        assertEquals(400, exception.getCode());
        verify(skuMapper, never()).selectById(11L);
    }

    private Product product(Long id, byte status) {
        Product product = new Product();
        product.setId(id);
        product.setName("Chair");
        product.setStatus(status);
        return product;
    }

    private Sku sku(Long id, Long productId, byte status, String specJson) {
        Sku sku = new Sku();
        sku.setId(id);
        sku.setProductId(productId);
        sku.setSkuCode("SKU-" + id);
        sku.setSpecJson(specJson);
        sku.setStatus(status);
        return sku;
    }
}
