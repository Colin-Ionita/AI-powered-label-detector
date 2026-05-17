package com.labelverifier.service;

import com.labelverifier.dto.GovernmentWarningResult;
import com.labelverifier.dto.MatchStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GovernmentWarningValidator {
    private final NormalizationService normalizationService;
    private final String canonicalWarning;
    private final boolean requireBoldMetadata;

    public GovernmentWarningValidator(
        NormalizationService normalizationService,
        @Value("${app.warning.require-bold-metadata:false}") boolean requireBoldMetadata
    ) {
        this.normalizationService = normalizationService;
        this.canonicalWarning = loadCanonicalWarning();
        this.requireBoldMetadata = requireBoldMetadata;
    }

    public GovernmentWarningResult validate(String extractedWarning, int confidence, boolean headingBoldDetected) {
        if (extractedWarning == null || extractedWarning.isBlank()) {
            return new GovernmentWarningResult(MatchStatus.MISSING, 0, null, "Government warning was not found.");
        }
        if (!extractedWarning.startsWith("GOVERNMENT WARNING:")) {
            if (extractedWarning.toLowerCase().startsWith("government warning:")) {
                return new GovernmentWarningResult(MatchStatus.MISMATCH, confidence, extractedWarning, "HEADING_CASE");
            }
            return new GovernmentWarningResult(MatchStatus.MISMATCH, confidence, extractedWarning, "Government warning heading is missing or malformed.");
        }
        double similarity = normalizationService.similarity(canonicalWarning, extractedWarning);
        if (similarity < 0.95) {
            return new GovernmentWarningResult(MatchStatus.MISMATCH, confidence, extractedWarning, "BODY_TEXT_CHANGED");
        }
        if (!headingBoldDetected && requireBoldMetadata) {
            return new GovernmentWarningResult(MatchStatus.LOW_CONFIDENCE, confidence, extractedWarning, "BOLD_UNVERIFIED");
        }
        return new GovernmentWarningResult(MatchStatus.MATCH, confidence, extractedWarning, "Canonical wording and heading checks passed.");
    }

    private String loadCanonicalWarning() {
        try {
            ClassPathResource resource = new ClassPathResource("regulatory/government-warning.txt");
            return resource.getContentAsString(StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Canonical government warning resource is missing.", ex);
        }
    }
}
