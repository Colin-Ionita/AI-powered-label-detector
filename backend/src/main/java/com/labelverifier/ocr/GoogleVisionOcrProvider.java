package com.labelverifier.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labelverifier.exception.OcrProviderException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "app.ocr.provider", havingValue = "google-vision")
public class GoogleVisionOcrProvider implements OcrProvider {
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String endpoint;

    public GoogleVisionOcrProvider(
        ObjectMapper objectMapper,
        @Value("${app.ocr.google-vision.api-key:}") String apiKey,
        @Value("${app.ocr.google-vision.endpoint}") String endpoint,
        @Value("${app.ocr.google-vision.timeout-ms}") int timeoutMs
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new OcrProviderException("GOOGLE_VISION_API_KEY is required when OCR_PROVIDER=google-vision.");
        }
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.endpoint = endpoint;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public OcrResult readText(String filename, byte[] imageBytes, String contentType) {
        try {
            String responseBody = restClient.post()
                .uri(visionUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody(imageBytes))
                .retrieve()
                .body(String.class);

            JsonNode response = firstResponse(responseBody);
            if (response.hasNonNull("error")) {
                String message = response.path("error").path("message").asText("Google Vision OCR request failed.");
                throw new OcrProviderException("Google Vision OCR failed: " + message);
            }

            String text = extractedText(response);
            return new OcrResult("google-vision", text, confidence(response, text), false);
        } catch (OcrProviderException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new OcrProviderException("Google Vision OCR request failed.", ex);
        } catch (Exception ex) {
            throw new OcrProviderException("Google Vision OCR response could not be parsed.", ex);
        }
    }

    private String visionUrl() {
        return UriComponentsBuilder.fromHttpUrl(endpoint)
            .queryParam("key", apiKey)
            .toUriString();
    }

    private Map<String, Object> requestBody(byte[] imageBytes) {
        return Map.of(
            "requests", List.of(Map.of(
                "image", Map.of("content", Base64.getEncoder().encodeToString(imageBytes)),
                "features", List.of(Map.of("type", "DOCUMENT_TEXT_DETECTION"))
            ))
        );
    }

    private JsonNode firstResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode responses = root.path("responses");
        if (!responses.isArray() || responses.isEmpty()) {
            throw new OcrProviderException("Google Vision OCR returned no response.");
        }
        return responses.get(0);
    }

    private String extractedText(JsonNode response) {
        String fullText = response.path("fullTextAnnotation").path("text").asText("");
        if (!fullText.isBlank()) {
            return fullText;
        }
        JsonNode firstAnnotation = response.path("textAnnotations").path(0);
        return firstAnnotation.path("description").asText("");
    }

    private int confidence(JsonNode response, String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        List<Double> values = new ArrayList<>();
        collectConfidences(response.path("fullTextAnnotation"), values);
        collectConfidences(response.path("textAnnotations"), values);
        if (values.isEmpty()) {
            return 75;
        }
        double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.75);
        if (average <= 1.0) {
            average *= 100.0;
        }
        return Math.max(0, Math.min(100, (int) Math.round(average)));
    }

    private void collectConfidences(JsonNode node, List<Double> values) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            JsonNode confidence = node.get("confidence");
            if (confidence != null && confidence.isNumber()) {
                values.add(confidence.asDouble());
            }
            node.fields().forEachRemaining(entry -> collectConfidences(entry.getValue(), values));
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectConfidences(child, values));
        }
    }
}
