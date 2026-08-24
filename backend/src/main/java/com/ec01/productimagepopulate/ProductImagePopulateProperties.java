package com.ec01.productimagepopulate;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "product-image.populate")
record ProductImagePopulateProperties(
        String sourceApiUrl,
        String objectPrefix,
        Path tempDirectory,
        int targetWidth,
        int maxDownloadBytes,
        int maxFileBytes,
        int requestDelayMillis,
        long productId,
        String productIds,
        int limit,
        boolean force
) {
}
