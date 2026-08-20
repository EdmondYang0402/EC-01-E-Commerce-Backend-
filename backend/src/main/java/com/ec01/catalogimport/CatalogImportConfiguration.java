package com.ec01.catalogimport;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("catalog-import")
@EnableConfigurationProperties(CatalogImportProperties.class)
class CatalogImportConfiguration {
}
