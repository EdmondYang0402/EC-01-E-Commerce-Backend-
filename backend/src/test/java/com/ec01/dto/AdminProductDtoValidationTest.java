package com.ec01.dto;

import com.ec01.dto.admin.product.ProductAdminQueryDTO;
import com.ec01.dto.admin.product.ProductCreateDTO;
import com.ec01.dto.admin.product.ProductUpdateDTO;
import com.ec01.dto.admin.product.SkuCreateDTO;
import com.ec01.dto.admin.product.SkuUpdateDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminProductDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void productCreateRequiresNameAndDoesNotExposeManagedFields() {
        ProductCreateDTO dto = new ProductCreateDTO();

        assertTrue(properties(validator.validate(dto)).contains("name"));
        Set<String> fields = Set.of(ProductCreateDTO.class.getDeclaredFields()).stream()
                .map(field -> field.getName())
                .collect(Collectors.toSet());
        assertFalse(fields.contains("id"));
        assertFalse(fields.contains("status"));
        assertFalse(fields.contains("createTime"));
        assertFalse(fields.contains("updateTime"));
    }

    @Test
    void productUpdateCannotChangeManagedFields() {
        Set<String> fields = Set.of(ProductUpdateDTO.class.getDeclaredFields()).stream()
                .map(field -> field.getName())
                .collect(Collectors.toSet());

        assertFalse(fields.contains("id"));
        assertFalse(fields.contains("status"));
        assertFalse(fields.contains("createTime"));
        assertFalse(fields.contains("updateTime"));
    }

    @Test
    void skuCreateAndUpdateRejectNegativePriceAndStock() {
        SkuCreateDTO create = new SkuCreateDTO();
        create.setSkuCode("CHAIR-BLACK");
        create.setSpecJson("{\"color\":\"black\"}");
        create.setPrice(new BigDecimal("-0.01"));
        create.setStock(-1);

        SkuUpdateDTO update = new SkuUpdateDTO();
        update.setSpecJson(create.getSpecJson());
        update.setPrice(create.getPrice());
        update.setStock(create.getStock());

        assertEquals(Set.of("price", "stock"), properties(validator.validate(create)));
        assertEquals(Set.of("price", "stock"), properties(validator.validate(update)));
    }

    @Test
    void adminQueryUsesPageDefaultsAndEnforcesSizeLimit() {
        ProductAdminQueryDTO dto = new ProductAdminQueryDTO();
        assertEquals(1, dto.getPage());
        assertEquals(20, dto.getSize());

        dto.setSize(101);
        assertEquals(Set.of("size"), properties(validator.validate(dto)));
    }

    private Set<String> properties(Set<? extends jakarta.validation.ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
