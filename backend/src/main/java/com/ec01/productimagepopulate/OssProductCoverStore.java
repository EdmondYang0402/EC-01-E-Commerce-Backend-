package com.ec01.productimagepopulate;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.ec01.config.OssProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;

@Component
@Profile("product-image-populate")
class OssProductCoverStore {

    private final OSS oss;
    private final OssProperties ossProperties;
    private final ProductImagePopulateProperties properties;
    private final RestClient restClient;

    OssProductCoverStore(
            OSS oss,
            OssProperties ossProperties,
            ProductImagePopulateProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.oss = oss;
        this.ossProperties = ossProperties;
        this.properties = properties;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "EC-01PortfolioImagePopulator/1.0 (local development project)")
                .build();
    }

    String upload(Long productId, ProcessedCover cover, ProductImageCandidate source) {
        String objectKey = "%s/%d/cover.jpg".formatted(
                trimSlashes(properties.objectPrefix()), productId);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(cover.bytes().length);
        metadata.setContentType("image/jpeg");
        metadata.setCacheControl("public, max-age=31536000, immutable");
        metadata.setContentDisposition("inline");
        metadata.addUserMetadata("image-source", "wikimedia-commons");
        metadata.addUserMetadata("source-page", source.sourcePage());
        metadata.addUserMetadata("license", source.license());

        oss.putObject(new PutObjectRequest(
                ossProperties.bucketName(),
                objectKey,
                new ByteArrayInputStream(cover.bytes()),
                metadata));
        oss.setObjectAcl(
                ossProperties.bucketName(),
                objectKey,
                CannedAccessControlList.PublicRead);
        return trimTrailingSlash(ossProperties.publicBaseUrl()) + "/" + objectKey;
    }

    void verifyPublicUrl(String url) {
        var response = restClient.head().uri(url).retrieve().toBodilessEntity();
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("OSS public URL returned " + response.getStatusCode());
        }
        String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalStateException("OSS public URL is not an image");
        }
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+|/+$", "");
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
