package com.ec01.productimagepopulate;

import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("product-image-populate")
class ProductImageKeywordCatalog {

    private final Map<Long, String> keywords = loadKeywords();

    String keywordFor(Long productId) {
        return keywords.get(productId);
    }

    private Map<Long, String> loadKeywords() {
        Map<Long, String> result = new HashMap<>();
        ClassPathResource resource = new ClassPathResource("product-image-keywords.tsv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] columns = line.split("\\t", 2);
                        if (columns.length != 2) {
                            throw new IllegalStateException("Invalid product image keyword row: " + line);
                        }
                        result.put(Long.valueOf(columns[0]), columns[1].trim());
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load product image keywords", exception);
        }
        return Map.copyOf(result);
    }
}
