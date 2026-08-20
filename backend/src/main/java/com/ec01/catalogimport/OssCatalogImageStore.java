package com.ec01.catalogimport;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.ec01.config.OssProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Component
@Profile("catalog-import")
class OssCatalogImageStore {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    private final OssProperties ossProperties;
    private final CatalogImportProperties importProperties;
    private final OSS oss;
    private final RestClient restClient;

    OssCatalogImageStore(
            OssProperties ossProperties,
            CatalogImportProperties importProperties,
            ObjectProvider<OSS> ossProvider,
            RestClient.Builder restClientBuilder
    ) {
        this.ossProperties = ossProperties;
        this.importProperties = importProperties;
        this.oss = ossProvider.getIfAvailable(() -> {
            throw new IllegalStateException(
                    "OSS credentials are missing. Set OSS_ACCESS_KEY_ID and OSS_ACCESS_KEY_SECRET before importing.");
        });
        this.restClient = restClientBuilder.build();
    }

    String storeCover(DummyJsonProduct product) {
        URI sourceUri = chooseImage(product);
        ResponseEntity<byte[]> response = restClient.get()
                .uri(sourceUri)
                .retrieve()
                .toEntity(byte[].class);

        byte[] bytes = response.getBody();
        MediaType contentType = response.getHeaders().getContentType();
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("Downloaded an empty image for source product " + product.id());
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalStateException("Image exceeds 10 MB for source product " + product.id());
        }
        if (contentType == null || !"image".equalsIgnoreCase(contentType.getType())) {
            throw new IllegalStateException("Source URL did not return an image for product " + product.id());
        }

        String objectKey = "%s/product-%04d/cover.%s".formatted(
                trimSlashes(importProperties.objectPrefix()),
                product.id(),
                extension(contentType));

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType.toString());
        metadata.setCacheControl("public, max-age=31536000, immutable");
        metadata.addUserMetadata("catalog-source", "dummyjson");
        metadata.addUserMetadata("source-product-id", String.valueOf(product.id()));

        PutObjectRequest request = new PutObjectRequest(
                ossProperties.bucketName(),
                objectKey,
                new ByteArrayInputStream(bytes),
                metadata);
        oss.putObject(request);
        return trimTrailingSlash(ossProperties.publicBaseUrl()) + "/" + objectKey;
    }

    private URI chooseImage(DummyJsonProduct product) {
        String image = product.thumbnail();
        if ((image == null || image.isBlank()) && product.images() != null) {
            image = product.images().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse(null);
        }
        if (image == null || image.isBlank()) {
            throw new IllegalArgumentException("Source product has no image: " + product.id());
        }
        URI uri = URI.create(image);
        if (uri.getScheme() == null
                || !List.of("https", "http").contains(uri.getScheme().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported image URL scheme for source product " + product.id());
        }
        return uri;
    }

    private String extension(MediaType contentType) {
        return switch (contentType.getSubtype().toLowerCase(Locale.ROOT)) {
            case "jpeg", "jpg" -> "jpg";
            case "png" -> "png";
            case "webp" -> "webp";
            case "avif" -> "avif";
            default -> "bin";
        };
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+|/+$", "");
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
