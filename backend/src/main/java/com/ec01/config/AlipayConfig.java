package com.ec01.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfig {

    @Bean
    @ConditionalOnProperty(prefix = "alipay", name = "enabled", havingValue = "true")
    AlipayClient alipayClient(AlipayProperties properties) {
        requireText(properties.getAppId(), "ALIPAY_APP_ID");
        requireText(properties.getGatewayUrl(), "ALIPAY_GATEWAY_URL");
        requireText(properties.getPrivateKey(), "ALIPAY_PRIVATE_KEY");
        requireText(properties.getAlipayPublicKey(), "ALIPAY_PUBLIC_KEY");
        requireText(properties.getNotifyUrl(), "ALIPAY_NOTIFY_URL");
        requireText(properties.getReturnUrl(), "ALIPAY_RETURN_URL");
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getPrivateKey(),
                "json",
                "UTF-8",
                properties.getAlipayPublicKey(),
                "RSA2"
        );
    }

    private void requireText(String value, String environmentName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(environmentName + " must be configured when ALIPAY_ENABLED=true");
        }
    }
}
