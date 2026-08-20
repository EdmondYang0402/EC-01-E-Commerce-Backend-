package com.ec01.catalogimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@Profile("catalog-import")
class DummyJsonCatalogClient {

    private final CatalogImportProperties properties;
    private final RestClient restClient;

    DummyJsonCatalogClient(CatalogImportProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    List<DummyJsonProduct> fetchProducts() {
        URI uri = UriComponentsBuilder.fromUri(properties.sourceUrl())
                .replaceQueryParam("limit", properties.limit())
                .replaceQueryParam("skip", 0)
                .build(true)
                .toUri();

        DummyJsonResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(DummyJsonResponse.class);

        if (response == null || response.products() == null) {
            throw new IllegalStateException("DummyJSON returned an empty product response");
        }
        return response.products();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DummyJsonResponse(List<DummyJsonProduct> products) {
    }
}
