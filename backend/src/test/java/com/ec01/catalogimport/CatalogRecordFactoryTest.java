package com.ec01.catalogimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogRecordFactoryTest {

    private final CatalogRecordFactory factory = new CatalogRecordFactory(
            new CatalogImportProperties(
                    URI.create("https://dummyjson.com/products"),
                    100,
                    "catalog/dummyjson",
                    new BigDecimal("7.20")),
            new ObjectMapper());

    @Test
    void createsActiveProductAndSkuUsingStableSourceIdentity() {
        DummyJsonProduct source = new DummyJsonProduct(
                12L,
                "Minimal Chair",
                "A compact lounge chair.",
                "home-decoration",
                new BigDecimal("19.99"),
                42,
                "EC Living",
                "CHAIR-RED",
                new BigDecimal("4.5"),
                "One year warranty",
                "Ships in two days",
                "https://example.com/chair.webp",
                List.of());

        CatalogProductDraft draft = factory.create(source, "https://bucket.example/catalog/chair.webp");

        assertEquals("Minimal Chair", draft.product().getName());
        assertEquals("https://bucket.example/catalog/chair.webp", draft.product().getCoverUrl());
        assertEquals((byte) 1, draft.product().getStatus());
        assertEquals("DJ-0012-CHAIR-RED", draft.sku().getSkuCode());
        assertEquals(new BigDecimal("143.93"), draft.sku().getPrice());
        assertEquals(42, draft.sku().getStock());
        assertTrue(draft.sku().getSpecJson().contains("home-decoration"));
    }
}
