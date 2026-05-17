package com.labelverifier.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health(@Value("${app.ocr.provider}") String ocrProvider) {
        return Map.of(
            "status", "UP",
            "service", "alcohol-label-verifier",
            "ocrProvider", ocrProvider,
            "time", Instant.now().toString()
        );
    }
}
