package com.ec01.catalogimport;

import com.ec01.mapper.ProductMapper;
import com.ec01.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("catalog-import")
@RequiredArgsConstructor
class CatalogProductWriter {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    boolean exists(String skuCode) {
        return skuMapper.selectBySkuCode(skuCode) != null;
    }

    @Transactional
    public void insert(CatalogProductDraft draft) {
        productMapper.insert(draft.product());
        draft.sku().setProductId(draft.product().getId());
        skuMapper.insert(draft.sku());
    }
}
