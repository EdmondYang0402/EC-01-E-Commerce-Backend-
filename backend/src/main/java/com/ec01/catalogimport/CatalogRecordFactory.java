package com.ec01.catalogimport;

import com.ec01.entity.Product;
import com.ec01.entity.Sku;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Profile("catalog-import")
class CatalogRecordFactory {

    private static final byte ACTIVE = 1;

    private final CatalogImportProperties properties;
    private final ObjectMapper objectMapper;

    CatalogRecordFactory(CatalogImportProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    String skuCode(DummyJsonProduct source) {
        String sourceSku = safe(source.sku()).replaceAll("[^A-Za-z0-9_-]", "");
        String code = "DJ-%04d-%s".formatted(source.id(), sourceSku).toUpperCase(Locale.ROOT);
        return code.substring(0, Math.min(code.length(), 64));
    }

    CatalogProductDraft create(DummyJsonProduct source, String coverUrl) {
        Product product = new Product();
        product.setName(truncate(required(source.title(), "title"), 120));
        product.setSubtitle(truncate(buildSubtitle(source), 255));
        product.setDescription(buildDescription(source));
        product.setCategoryId(categoryId(source.category()));
        product.setCoverUrl(coverUrl);
        product.setStatus(ACTIVE);

        Sku sku = new Sku();
        sku.setSkuCode(skuCode(source));
        sku.setSpecJson(buildSpecJson(source));
        sku.setPrice(price(source.price()));
        sku.setStock(Math.max(source.stock() == null ? 0 : source.stock(), 0));
        sku.setStatus(ACTIVE);
        return new CatalogProductDraft(product, sku);
    }

    private String buildSubtitle(DummyJsonProduct source) {
        String brand = safe(source.brand());
        String category = safe(source.category()).replace('-', ' ');
        if (!brand.isBlank() && !category.isBlank()) {
            return "%s · %s · 开发演示商品".formatted(brand, category);
        }
        return category.isBlank() ? "开发演示商品" : category + " · 开发演示商品";
    }

    private String buildDescription(DummyJsonProduct source) {
        StringBuilder description = new StringBuilder(safe(source.description()));
        appendLine(description, source.shippingInformation());
        appendLine(description, source.warrantyInformation());
        return description.toString();
    }

    private void appendLine(StringBuilder target, String value) {
        if (value != null && !value.isBlank()) {
            if (!target.isEmpty()) {
                target.append(System.lineSeparator());
            }
            target.append(value.trim());
        }
    }

    private String buildSpecJson(DummyJsonProduct source) {
        Map<String, Object> spec = new LinkedHashMap<>();
        putIfPresent(spec, "brand", source.brand());
        putIfPresent(spec, "category", source.category());
        putIfPresent(spec, "sourceSku", source.sku());
        if (source.rating() != null) {
            spec.put("rating", source.rating());
        }
        try {
            return truncate(objectMapper.writeValueAsString(spec), 500);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize SKU specification", exception);
        }
    }

    private void putIfPresent(Map<String, Object> target, String key, String value) {
        if (value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }

    private BigDecimal price(BigDecimal sourcePrice) {
        BigDecimal positivePrice = sourcePrice == null || sourcePrice.signum() < 0
                ? BigDecimal.ZERO
                : sourcePrice;
        return positivePrice.multiply(properties.currencyMultiplier()).setScale(2, RoundingMode.HALF_UP);
    }

    private long categoryId(String category) {
        long unsignedHash = Integer.toUnsignedLong(safe(category).toLowerCase(Locale.ROOT).hashCode());
        return unsignedHash % 10_000 + 1;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Source product is missing " + field);
        }
        return value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
