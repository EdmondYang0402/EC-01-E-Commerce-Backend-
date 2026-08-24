package com.ec01.productimagepopulate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
class WikimediaCommonsImageSource {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png");
    private static final List<String> REJECTED_TITLE_PARTS = List.of(
            " logo", " icon", " diagram", " schematic", " broken", " repair",
            " manual", " advertisement", " patent", " drawing");

    private final ProductImagePopulateProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    WikimediaCommonsImageSource(
            ProductImagePopulateProperties properties,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "EC-01PortfolioImagePopulator/1.0 (local development project)")
                .build();
    }

    List<ProductImageCandidate> search(String keyword) throws InterruptedException {
        URI uri = UriComponentsBuilder.fromUriString(properties.sourceApiUrl())
                .queryParam("action", "query")
                .queryParam("generator", "search")
                .queryParam("gsrsearch", keyword + " filetype:bitmap")
                .queryParam("gsrnamespace", 6)
                .queryParam("gsrlimit", 30)
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url|size|mime|extmetadata")
                .queryParam("iiurlwidth", 1200)
                .queryParam("format", "json")
                .queryParam("formatversion", 2)
                .build()
                .encode()
                .toUri();

        String body = requestWithBackoff(uri);
        if (properties.requestDelayMillis() > 0) {
            Thread.sleep(properties.requestDelayMillis());
        }
        if (body == null || body.isBlank()) {
            return List.of();
        }

        try {
            return candidates(objectMapper.readTree(body));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot parse Wikimedia Commons response", exception);
        }
    }

    private String requestWithBackoff(URI uri) throws InterruptedException {
        RestClientResponseException lastRateLimit = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                return restClient.get().uri(uri).retrieve().body(String.class);
            } catch (RestClientResponseException exception) {
                if (exception.getStatusCode().value() != 429) {
                    throw exception;
                }
                lastRateLimit = exception;
                long waitSeconds = retryAfterSeconds(exception, attempt);
                Thread.sleep(waitSeconds * 1000L);
            }
        }
        throw lastRateLimit;
    }

    private long retryAfterSeconds(RestClientResponseException exception, int attempt) {
        String retryAfter = exception.getResponseHeaders() == null
                ? null
                : exception.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter != null) {
            try {
                return Math.clamp(Long.parseLong(retryAfter), 1, 60);
            } catch (NumberFormatException ignored) {
                // HTTP-date Retry-After values fall back to bounded exponential delay.
            }
        }
        return Math.min(60, 10L << attempt);
    }

    private List<ProductImageCandidate> candidates(JsonNode root) {
        JsonNode pages = root.path("query").path("pages");
        if (!pages.isArray()) {
            return List.of();
        }

        List<ProductImageCandidate> result = new ArrayList<>();
        for (JsonNode page : pages) {
            JsonNode info = page.path("imageinfo").path(0);
            String title = page.path("title").asText("");
            String mime = info.path("mime").asText("");
            int width = info.path("width").asInt(0);
            int height = info.path("height").asInt(0);
            String imageUrl = info.path("thumburl").asText(info.path("url").asText(""));
            if (!isUsable(title, mime, width, height, imageUrl)) {
                continue;
            }

            JsonNode metadata = info.path("extmetadata");
            String license = metadataValue(metadata, "LicenseShortName");
            if (!isOpenLicense(license)) {
                continue;
            }
            result.add(new ProductImageCandidate(
                    title,
                    imageUrl,
                    info.path("descriptionurl").asText(""),
                    stripHtml(metadataValue(metadata, "Artist")),
                    license,
                    metadataValue(metadata, "LicenseUrl"),
                    width,
                    height));
        }
        return result;
    }

    private boolean isUsable(String title, String mime, int width, int height, String imageUrl) {
        if (!ALLOWED_MIME_TYPES.contains(mime) || width < 800 || height < 500 || imageUrl.isBlank()) {
            return false;
        }
        double ratio = (double) width / height;
        if (ratio < 0.55 || ratio > 2.2) {
            return false;
        }
        String normalizedTitle = " " + title.toLowerCase(Locale.ROOT);
        return REJECTED_TITLE_PARTS.stream().noneMatch(normalizedTitle::contains);
    }

    private boolean isOpenLicense(String license) {
        String normalized = license.toUpperCase(Locale.ROOT);
        return normalized.startsWith("CC BY")
                || normalized.startsWith("CC0")
                || normalized.contains("PUBLIC DOMAIN")
                || normalized.equals("PDM")
                || normalized.equals("FAL");
    }

    private String metadataValue(JsonNode metadata, String field) {
        return metadata.path(field).path("value").asText("");
    }

    private String stripHtml(String value) {
        return value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
