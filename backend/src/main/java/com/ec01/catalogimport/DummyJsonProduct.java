package com.ec01.catalogimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonProduct(
        Long id,
        String title,
        String description,
        String category,
        BigDecimal price,
        Integer stock,
        String brand,
        String sku,
        BigDecimal rating,
        String warrantyInformation,
        String shippingInformation,
        String thumbnail,
        List<String> images
) {
}
