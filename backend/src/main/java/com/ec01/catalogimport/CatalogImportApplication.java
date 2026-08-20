package com.ec01.catalogimport;

import com.ec01.config.OssConfig;
import com.ec01.mapper.ProductMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = CatalogImportService.class)
@MapperScan(basePackageClasses = ProductMapper.class)
@Import(OssConfig.class)
@Profile("catalog-import")
public final class CatalogImportApplication {

    private CatalogImportApplication() {
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CatalogImportApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setAdditionalProfiles("catalog-import");

        try (ConfigurableApplicationContext context = application.run(args)) {
            CatalogImportReport report = context.getBean(CatalogImportService.class).importCatalog();
            System.out.printf(
                    "Catalog import completed: requested=%d, imported=%d, skipped=%d%n",
                    report.requested(), report.imported(), report.skipped());
        }
    }
}
