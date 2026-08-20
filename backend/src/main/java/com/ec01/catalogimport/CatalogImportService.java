package com.ec01.catalogimport;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("catalog-import")
@RequiredArgsConstructor
class CatalogImportService {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportService.class);

    private final DummyJsonCatalogClient catalogClient;
    private final CatalogRecordFactory recordFactory;
    private final OssCatalogImageStore imageStore;
    private final CatalogProductWriter productWriter;

    CatalogImportReport importCatalog() {
        List<DummyJsonProduct> products = catalogClient.fetchProducts();
        List<Long> failedIds = new ArrayList<>();
        int imported = 0;
        int skipped = 0;

        for (DummyJsonProduct source : products) {
            String skuCode = recordFactory.skuCode(source);
            if (productWriter.exists(skuCode)) {
                skipped++;
                continue;
            }

            try {
                String coverUrl = imageStore.storeCover(source);
                productWriter.insert(recordFactory.create(source, coverUrl));
                imported++;
                if (imported % 10 == 0) {
                    log.info("Imported {} catalog products", imported);
                }
            } catch (RuntimeException exception) {
                failedIds.add(source.id());
                log.warn("Catalog product {} failed: {}", source.id(), exception.getMessage());
            }
        }

        if (!failedIds.isEmpty()) {
            throw new IllegalStateException(
                    "Catalog import completed with failures for source product IDs: " + failedIds);
        }
        return new CatalogImportReport(products.size(), imported, skipped);
    }
}
