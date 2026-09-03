package com.ec01.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    private boolean enabled;
    private String appId;
    private String gatewayUrl;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String sellerId;
}
