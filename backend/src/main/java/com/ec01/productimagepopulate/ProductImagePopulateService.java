package com.ec01.productimagepopulate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class ProductImagePopulateService {

    private static final Logger log = LoggerFactory.getLogger(ProductImagePopulateService.class);
    private static final String MISSING_COVER_SQL = """
            (cover_url IS NULL OR TRIM(cover_url) = '' OR cover_url LIKE 'https://placehold.co/%')
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ProductImagePopulateProperties properties;
    private final ProductImageKeywordCatalog keywordCatalog;
    private final WikimediaCommonsImageSource imageSource;
    private final ProductCoverProcessor coverProcessor;
    private final OssProductCoverStore coverStore;

    ProductImagePopulateService(
            JdbcTemplate jdbcTemplate,
            ProductImagePopulateProperties properties,
            ProductImageKeywordCatalog keywordCatalog,
            WikimediaCommonsImageSource imageSource,
            ProductCoverProcessor coverProcessor,
            OssProductCoverStore coverStore
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.keywordCatalog = keywordCatalog;
        this.imageSource = imageSource;
        this.coverProcessor = coverProcessor;
        this.coverStore = coverStore;
    }

    ProductImagePopulateReport populate() {
        List<ProductRow> products = findMissingProducts();
        List<ProductImageFailure> failures = new ArrayList<>();
        Set<String> usedSourceTitles = new HashSet<>();
        int uploaded = 0;

        prepareReportFiles();
        for (ProductRow product : products) {
            String keyword = keywordCatalog.keywordFor(product.id());
            if (keyword == null || keyword.isBlank()) {
                ProductImageFailure failure = new ProductImageFailure(
                        product.id(), product.name(), "", "No reviewed search keyword");
                failures.add(failure);
                appendFailure(failure);
                continue;
            }

            try {
                UploadResult result = tryCandidates(product, keyword, usedSourceTitles);
                appendAttribution(product, keyword, result.candidate(), result.cover(), result.ossUrl());
                uploaded++;
                log.info("Product {} image uploaded to {}", product.id(), result.ossUrl());
            } catch (Exception exception) {
                ProductImageFailure failure = new ProductImageFailure(
                        product.id(), product.name(), keyword, conciseMessage(exception));
                failures.add(failure);
                appendFailure(failure);
                log.warn("Product {} image failed: {}", product.id(), failure.reason());
            }
        }
        return new ProductImagePopulateReport(products.size(), uploaded, List.copyOf(failures));
    }

    private UploadResult tryCandidates(
            ProductRow product,
            String keyword,
            Set<String> usedSourceTitles
    ) throws Exception {
        List<ProductImageCandidate> candidates = imageSource.search(keyword).stream()
                .filter(item -> usedSourceTitles.add(item.title()))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No suitable licensed image found");
        }

        Exception lastFailure = null;
        for (ProductImageCandidate candidate : candidates) {
            try {
                ProcessedCover cover = coverProcessor.downloadAndProcess(product.id(), candidate);
                String ossUrl = coverStore.upload(product.id(), cover, candidate);
                coverStore.verifyPublicUrl(ossUrl);
                String updateSql = """
                        UPDATE product
                        SET cover_url = ?, update_time = NOW()
                        WHERE id = ?
                        """ + (properties.force() ? "" : " AND " + MISSING_COVER_SQL);
                int updated = jdbcTemplate.update(updateSql, ossUrl, product.id());
                if (updated != 1) {
                    throw new IllegalStateException(
                            "Product row was not updated; cover may have changed concurrently");
                }
                return new UploadResult(candidate, cover, ossUrl);
            } catch (Exception exception) {
                lastFailure = exception;
                log.info("Candidate {} failed for product {}: {}",
                        candidate.title(), product.id(), conciseMessage(exception));
                Thread.sleep(Math.max(1000, properties.requestDelayMillis()));
            }
        }
        throw new IllegalStateException("All licensed candidates failed", lastFailure);
    }

    private List<ProductRow> findMissingProducts() {
        String sql = """
                SELECT id, name, description, category_id, cover_url
                FROM product
                WHERE %s
                """.formatted(properties.force() ? "1 = 1" : MISSING_COVER_SQL);
        List<Object> parameters = new ArrayList<>();
        if (properties.productId() > 0) {
            sql += " AND id = ?";
            parameters.add(properties.productId());
        }
        sql += " ORDER BY id";
        List<ProductRow> products = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new ProductRow(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getObject("category_id", Long.class),
                resultSet.getString("cover_url")), parameters.toArray());
        if (properties.productIds() != null && !properties.productIds().isBlank()) {
            Set<Long> selectedIds = new HashSet<>(Arrays.stream(properties.productIds().split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(Long::valueOf)
                    .toList());
            products = products.stream().filter(product -> selectedIds.contains(product.id())).toList();
        }
        if (properties.limit() > 0 && products.size() > properties.limit()) {
            return new ArrayList<>(products.subList(0, properties.limit()));
        }
        return products;
    }

    private void prepareReportFiles() {
        try {
            Files.createDirectories(properties.tempDirectory());
            writeHeaderIfMissing(attributionPath(),
                    "productId,productName,keyword,sourceTitle,creator,license,licenseUrl,sourcePage,ossUrl,width,height,bytes\n");
            writeHeaderIfMissing(failurePath(),
                    "productId,productName,keyword,failureReason\n");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot prepare image population reports", exception);
        }
    }

    private void writeHeaderIfMissing(Path path, String header) throws IOException {
        if (Files.notExists(path) || Files.size(path) == 0) {
            Files.writeString(path, header, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void appendAttribution(
            ProductRow product,
            String keyword,
            ProductImageCandidate candidate,
            ProcessedCover cover,
            String ossUrl
    ) throws IOException {
        appendCsv(attributionPath(), List.of(
                String.valueOf(product.id()), product.name(), keyword, candidate.title(),
                candidate.creator(), candidate.license(), candidate.licenseUrl(),
                candidate.sourcePage(), ossUrl, String.valueOf(cover.width()),
                String.valueOf(cover.height()), String.valueOf(cover.bytes().length)));
    }

    private void appendFailure(ProductImageFailure failure) {
        try {
            appendCsv(failurePath(), List.of(
                    String.valueOf(failure.productId()), failure.productName(),
                    failure.keyword(), failure.reason()));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot append failure report", exception);
        }
    }

    private void appendCsv(Path path, List<String> values) throws IOException {
        String row = values.stream().map(this::csv).reduce((left, right) -> left + "," + right).orElse("");
        Files.writeString(path, row + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String conciseMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }
        String compact = message.replaceAll("\\s+", " ").trim();
        return compact.length() <= 300 ? compact : compact.substring(0, 300) + "...";
    }

    private Path attributionPath() {
        return properties.tempDirectory().resolve("attributions.csv");
    }

    private Path failurePath() {
        return properties.tempDirectory().resolve("failures.csv");
    }
}

record ProductRow(Long id, String name, String description, Long categoryId, String coverUrl) {
}

record UploadResult(ProductImageCandidate candidate, ProcessedCover cover, String ossUrl) {
}
