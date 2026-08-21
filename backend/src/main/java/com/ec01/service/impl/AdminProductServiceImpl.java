package com.ec01.service.impl;

import com.ec01.common.PageResult;
import com.ec01.common.ProductStatus;
import com.ec01.common.SkuStatus;
import com.ec01.dto.admin.product.ProductAdminQueryDTO;
import com.ec01.dto.admin.product.ProductCreateDTO;
import com.ec01.dto.admin.product.ProductStatusUpdateDTO;
import com.ec01.dto.admin.product.ProductUpdateDTO;
import com.ec01.dto.admin.product.SkuCreateDTO;
import com.ec01.dto.admin.product.SkuStatusUpdateDTO;
import com.ec01.dto.admin.product.SkuUpdateDTO;
import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import com.ec01.exception.BusinessException;
import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import com.ec01.service.AdminProductService;
import com.ec01.vo.admin.product.AdminProductDetailVO;
import com.ec01.vo.admin.product.AdminProductListVO;
import com.ec01.vo.admin.product.AdminSkuVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final ProductServiceImpl productService;

    public AdminProductServiceImpl(
            ProductMapper productMapper,
            SkuMapper skuMapper,
            ProductServiceImpl productService) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.productService = productService;
    }

    @Override
    public PageResult<AdminProductListVO> getProductPage(ProductAdminQueryDTO dto) {
        validatePage(dto);
        String keyword = normalizeKeyword(dto.getKeyword());
        Byte status = dto.getStatus() == null ? null : dto.getStatus().getCode();
        long offset = (long) (dto.getPage() - 1) * dto.getSize();

        List<AdminProductListVO> records = productMapper.selectAdminPage(
                        keyword, status, dto.getCategoryId(), offset, dto.getSize())
                .stream()
                .map(this::toAdminProductListVO)
                .toList();
        long total = productMapper.countAdminProducts(keyword, status, dto.getCategoryId());
        return new PageResult<>(records, total);
    }

    @Override
    public AdminProductDetailVO getProductDetail(Long productId) {
        Product product = requireProduct(productId);
        AdminProductDetailVO vo = new AdminProductDetailVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setCoverUrl(product.getCoverUrl());
        vo.setDescription(product.getDescription());
        vo.setCategoryId(product.getCategoryId());
        vo.setStatus(toProductStatus(product.getStatus()));
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());
        vo.setSkus(skuMapper.selectAllByProductId(productId).stream()
                .map(this::toAdminSkuVO)
                .toList());
        return vo;
    }

    @Override
    public Long createProduct(ProductCreateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException(400, "商品名称不能为空");
        }
        productService.validateProductCategory(dto.getCategoryId());
        Product product = new Product();

        product.setName(dto.getName().trim());
        product.setSubtitle(dto.getSubtitle());
        product.setDescription(dto.getDescription());
        product.setCoverUrl(dto.getCoverUrl());
        product.setCategoryId(dto.getCategoryId());
        product.setStatus(ProductStatus.OFF_SHELF.getCode());

        if (productMapper.insert(product) <= 0) {
            throw new BusinessException("商品创建失败");
        }

        return product.getId();
    }

    @Override
    public void updateProduct(Long productId, ProductUpdateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException(400, "商品基础信息不合法");
        }
        productService.validateProductCategory(dto.getCategoryId());
        Product product = requireProduct(productId);
        product.setName(dto.getName().trim());
        product.setSubtitle(dto.getSubtitle());
        product.setCoverUrl(dto.getCoverUrl());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        if (productMapper.update(product) <= 0) {
            throw new BusinessException(500, "商品信息更新失败");
        }
    }

    @Override
    public void changeStatus(Long productId, ProductStatusUpdateDTO dto) {
        if (dto == null || dto.getStatus() == null) {
            throw new BusinessException(400, "商品状态不能为空");
        }
        Product product = requireProduct(productId);
        byte targetStatus = dto.getStatus().getCode();
        if (product.getStatus() == targetStatus) {
            return;
        }
        if (dto.getStatus() == ProductStatus.ON_SHELF) {
            boolean hasEnabledSku = skuMapper.selectAllByProductId(productId).stream()
                    .anyMatch(sku -> sku.getStatus() != null
                            && sku.getStatus() == SkuStatus.ENABLED.getCode());
            if (!hasEnabledSku) {
                throw new BusinessException(409, "商品至少需要一个已启用SKU才能上架");
            }
        }
        if (productMapper.updateStatus(productId, targetStatus) <= 0) {
            throw new BusinessException(500, "商品状态更新失败");
        }
    }

    @Override
    public Long addSku(Long productId, SkuCreateDTO dto) {
        requireProduct(productId);
        if (dto == null || dto.getSkuCode() == null || dto.getSkuCode().isBlank()
                || dto.getSpecJson() == null || dto.getSpecJson().isBlank()
                || dto.getPrice() == null || dto.getPrice().signum() <= 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new BusinessException(400, "SKU信息不合法");
        }
        if (skuMapper.selectBySkuCode(dto.getSkuCode().trim()) != null) {
            throw new BusinessException(409, "SKU编码已存在");
        }

        String specJson = dto.getSpecJson().trim();
        if (skuMapper.selectByProductIdAndSpecJson(productId, specJson) != null) {
            throw new BusinessException(409, "同一商品下该SKU规格已存在");
        }

        Sku sku = new Sku();

        sku.setProductId(productId);
        sku.setSkuCode(dto.getSkuCode().trim());
        sku.setSpecJson(specJson);
        sku.setPrice(dto.getPrice());
        sku.setStock(dto.getStock());
        sku.setStatus(SkuStatus.ENABLED.getCode());

        if (skuMapper.insert(sku) <= 0) {
            throw new BusinessException("SKU创建失败");
        }

        return sku.getId();
    }

    @Override
    public void updateSku(Long skuId, SkuUpdateDTO dto) {
        if (skuId == null || skuId <= 0 || dto == null
                || dto.getSpecJson() == null || dto.getSpecJson().isBlank()
                || dto.getPrice() == null || dto.getPrice().signum() <= 0
                || dto.getStock() == null || dto.getStock() < 0) {
            throw new BusinessException(400, "SKU基础信息不合法");
        }
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }
        String specJson = dto.getSpecJson().trim();
        Sku duplicate = skuMapper.selectByProductIdAndSpecJson(sku.getProductId(), specJson);
        if (duplicate != null && !duplicate.getId().equals(skuId)) {
            throw new BusinessException(409, "同一商品下该SKU规格已存在");
        }
        sku.setSpecJson(specJson);
        sku.setPrice(dto.getPrice());
        sku.setStock(dto.getStock());
        if (skuMapper.update(sku) <= 0) {
            throw new BusinessException(500, "SKU信息更新失败");
        }
    }

    @Override
    public void changeSkuStatus(Long skuId, SkuStatusUpdateDTO dto) {
        if (skuId == null || skuId <= 0 || dto == null || dto.getStatus() == null) {
            throw new BusinessException(400, "SKU状态参数不合法");
        }
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }
        Product product = requireProduct(sku.getProductId());
        byte targetStatus = dto.getStatus().getCode();
        if (sku.getStatus() == targetStatus) {
            return;
        }
        if (dto.getStatus() == SkuStatus.DISABLED
                && product.getStatus() == ProductStatus.ON_SHELF.getCode()) {
            long enabledCount = skuMapper.selectAllByProductId(product.getId()).stream()
                    .filter(item -> item.getStatus() != null
                            && item.getStatus() == SkuStatus.ENABLED.getCode())
                    .count();
            if (enabledCount <= 1) {
                throw new BusinessException(409, "上架商品必须保留至少一个已启用SKU");
            }
        }
        if (skuMapper.updateStatus(skuId, targetStatus) <= 0) {
            throw new BusinessException(500, "SKU状态更新失败");
        }
    }

    private void validatePage(ProductAdminQueryDTO dto) {
        if (dto == null || dto.getPage() == null || dto.getSize() == null
                || dto.getPage() < 1 || dto.getSize() < 1 || dto.getSize() > 100) {
            throw new BusinessException(400, "分页参数不合法");
        }
    }

    private Product requireProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(400, "商品ID不合法");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private AdminProductListVO toAdminProductListVO(Product product) {
        AdminProductListVO vo = new AdminProductListVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setCoverUrl(product.getCoverUrl());
        vo.setStatus(toProductStatus(product.getStatus()));
        vo.setCategoryId(product.getCategoryId());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());
        return vo;
    }

    private AdminSkuVO toAdminSkuVO(Sku sku) {
        AdminSkuVO vo = new AdminSkuVO();
        vo.setId(sku.getId());
        vo.setProductId(sku.getProductId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpecJson(sku.getSpecJson());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setStatus(toSkuStatus(sku.getStatus()));
        vo.setCreateTime(sku.getCreateTime());
        vo.setUpdateTime(sku.getUpdateTime());
        return vo;
    }

    private ProductStatus toProductStatus(Byte code) {
        for (ProductStatus status : ProductStatus.values()) {
            if (code != null && status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(500, "商品状态数据异常");
    }

    private SkuStatus toSkuStatus(Byte code) {
        for (SkuStatus status : SkuStatus.values()) {
            if (code != null && status.getCode() == code) {
                return status;
            }
        }
        throw new BusinessException(500, "SKU状态数据异常");
    }
}
