package com.ec01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oss")
public record OssProperties(
        String region,
        String endpoint,
        String bucketName,
        String publicBaseUrl
) {
}
