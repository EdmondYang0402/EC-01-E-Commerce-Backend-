package com.ec01.productimagepopulate;

import com.ec01.config.OssConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = ProductImagePopulateService.class)
@Import(OssConfig.class)
@Profile("product-image-populate")
public final class ProductImagePopulateApplication {

    private ProductImagePopulateApplication() {
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ProductImagePopulateApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setAdditionalProfiles("product-image-populate");

        try (ConfigurableApplicationContext context = application.run(args)) {
            ProductImagePopulateReport report = context
                    .getBean(ProductImagePopulateService.class)
                    .populate();
            System.out.printf(
                    "Product image population completed: discovered=%d, uploaded=%d, failed=%d%n",
                    report.discovered(), report.uploaded(), report.failures().size());
            report.failures().forEach(failure -> System.out.printf(
                    "FAILED productId=%d name=%s keyword=%s reason=%s%n",
                    failure.productId(), failure.productName(), failure.keyword(), failure.reason()));
        }
    }
}
