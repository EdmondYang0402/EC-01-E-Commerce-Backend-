package com.ec01.catalogimport;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "catalog.import")
public record CatalogImportProperties(
        @NotNull URI sourceUrl,
        @Min(1) @Max(100) int limit,
        @NotBlank String objectPrefix,
        @NotNull @DecimalMin("0.01") BigDecimal currencyMultiplier
) {
}
