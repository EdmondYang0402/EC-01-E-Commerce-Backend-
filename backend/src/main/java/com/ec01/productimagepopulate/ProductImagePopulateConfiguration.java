package com.ec01.productimagepopulate;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("product-image-populate")
@EnableConfigurationProperties(ProductImagePopulateProperties.class)
class ProductImagePopulateConfiguration {
}
