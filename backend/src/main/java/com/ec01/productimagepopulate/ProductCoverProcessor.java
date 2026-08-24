package com.ec01.productimagepopulate;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.util.Iterator;

@Component
class ProductCoverProcessor {

    private final ProductImagePopulateProperties properties;
    private final RestClient restClient;

    ProductCoverProcessor(
            ProductImagePopulateProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "EC-01PortfolioImagePopulator/1.0 (local development project)")
                .defaultHeader(HttpHeaders.REFERER, "https://commons.wikimedia.org/")
                .defaultHeader(HttpHeaders.ACCEPT, "image/jpeg,image/png,image/*;q=0.8")
                .build();
    }

    ProcessedCover downloadAndProcess(Long productId, ProductImageCandidate candidate) throws IOException {
        byte[] source = restClient.get()
                .uri(URI.create(candidate.imageUrl()))
                .retrieve()
                .body(byte[].class);
        if (source == null || source.length == 0) {
            throw new IllegalStateException("Downloaded image is empty");
        }
        if (source.length > properties.maxDownloadBytes()) {
            throw new IllegalStateException("Downloaded image exceeds configured limit");
        }

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(source));
        if (original == null) {
            throw new IllegalStateException("Downloaded content is not a supported image");
        }
        BufferedImage rendered = renderRgb(original);
        byte[] jpeg = encodeWithinLimit(rendered);

        Path directory = properties.tempDirectory().resolve(String.valueOf(productId));
        Files.createDirectories(directory);
        Path output = directory.resolve("cover.jpg");
        Files.write(output, jpeg);
        return new ProcessedCover(output, jpeg, rendered.getWidth(), rendered.getHeight());
    }

    private BufferedImage renderRgb(BufferedImage original) {
        double scale = original.getWidth() > properties.targetWidth()
                ? (double) properties.targetWidth() / original.getWidth()
                : 1.0;
        int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
        BufferedImage rendered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rendered.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(original, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return rendered;
    }

    private byte[] encodeWithinLimit(BufferedImage image) throws IOException {
        for (float quality = 0.86f; quality >= 0.51f; quality -= 0.07f) {
            byte[] bytes = encodeJpeg(image, quality);
            if (bytes.length <= properties.maxFileBytes()) {
                return bytes;
            }
        }
        throw new IllegalStateException("Processed image still exceeds configured limit");
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageIO writer is available");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(quality);
            parameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            writer.write(null, new IIOImage(image, null, null), parameters);
            writer.dispose();
            return bytes.toByteArray();
        } finally {
            writer.dispose();
        }
    }
}

record ProcessedCover(Path path, byte[] bytes, int width, int height) {
}
