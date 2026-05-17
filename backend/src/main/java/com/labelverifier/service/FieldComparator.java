package com.labelverifier.service;

import com.labelverifier.dto.FieldMatchResult;
import com.labelverifier.dto.MatchStatus;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class FieldComparator {
    private static final Pattern PERCENT = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%");
    private static final Pattern PROOF = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*proof", Pattern.CASE_INSENSITIVE);
    private static final Pattern NET = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ml|mL|ML|l|L|fl\\.?\\s*oz)", Pattern.CASE_INSENSITIVE);

    private final NormalizationService normalizationService;

    public FieldComparator(NormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }

    public FieldMatchResult text(String key, String display, String expected, String extracted, int confidence, double threshold) {
        if (isBlank(expected)) {
            return new FieldMatchResult(key, display, expected, extracted, MatchStatus.NOT_APPLICABLE, 100, "No expected value supplied.");
        }
        if (isBlank(extracted)) {
            return new FieldMatchResult(key, display, expected, extracted, MatchStatus.MISSING, 0, "Field was not found in OCR text.");
        }
        double similarity = normalizationService.similarity(expected, extracted);
        MatchStatus status = similarity >= threshold ? MatchStatus.MATCH : MatchStatus.MISMATCH;
        String reason = status == MatchStatus.MATCH
            ? "Normalized text matches."
            : "Extracted text differs from the expected application value.";
        return new FieldMatchResult(key, display, expected, extracted, status, confidence, reason);
    }

    public FieldMatchResult alcohol(String expected, String extracted, int confidence) {
        Double expectedAbv = extractAbv(expected);
        Double extractedAbv = extractAbv(extracted);
        if (expectedAbv == null) {
            return new FieldMatchResult("alcoholContent", "Alcohol content", expected, extracted, MatchStatus.NOT_APPLICABLE, 100, "No expected ABV supplied.");
        }
        if (extractedAbv == null) {
            return new FieldMatchResult("alcoholContent", "Alcohol content", expected, extracted, MatchStatus.MISSING, 0, "Alcohol content was not found in OCR text.");
        }
        double delta = Math.abs(expectedAbv - extractedAbv);
        if (delta <= 0.5) {
            Double extractedProof = extractProof(extracted);
            if (extractedProof != null && Math.abs(extractedProof - extractedAbv * 2) > 1.0) {
                return new FieldMatchResult("alcoholContent", "Alcohol content", expected, extracted, MatchStatus.LOW_CONFIDENCE, confidence, "ABV matches, but proof does not appear consistent.");
            }
            return new FieldMatchResult("alcoholContent", "Alcohol content", expected, extracted, MatchStatus.MATCH, confidence, "ABV is within 0.5 percentage points.");
        }
        return new FieldMatchResult("alcoholContent", "Alcohol content", expected, extracted, MatchStatus.MISMATCH, confidence, "ABV differs by " + round(delta) + " percentage points.");
    }

    public FieldMatchResult netContents(String expected, String extracted, int confidence) {
        Double expectedMl = extractMilliliters(expected);
        Double extractedMl = extractMilliliters(extracted);
        if (expectedMl == null) {
            return new FieldMatchResult("netContents", "Net contents", expected, extracted, MatchStatus.NOT_APPLICABLE, 100, "No expected net contents supplied.");
        }
        if (extractedMl == null) {
            return new FieldMatchResult("netContents", "Net contents", expected, extracted, MatchStatus.MISSING, 0, "Net contents were not found in OCR text.");
        }
        double delta = Math.abs(expectedMl - extractedMl);
        if (delta <= 5.0) {
            return new FieldMatchResult("netContents", "Net contents", expected, extracted, MatchStatus.MATCH, confidence, "Net contents normalize to the same milliliter quantity.");
        }
        return new FieldMatchResult("netContents", "Net contents", expected, extracted, MatchStatus.MISMATCH, confidence, "Net contents differ after unit normalization.");
    }

    private Double extractAbv(String value) {
        if (value == null) {
            return null;
        }
        Matcher percent = PERCENT.matcher(value);
        if (percent.find()) {
            return Double.parseDouble(percent.group(1));
        }
        Double proof = extractProof(value);
        return proof == null ? null : proof / 2.0;
    }

    private Double extractProof(String value) {
        if (value == null) {
            return null;
        }
        Matcher proof = PROOF.matcher(value);
        return proof.find() ? Double.parseDouble(proof.group(1)) : null;
    }

    private Double extractMilliliters(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = NET.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        double amount = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).replace(".", "").replaceAll("\\s+", "").toLowerCase();
        return switch (unit) {
            case "ml" -> amount;
            case "l" -> amount * 1000;
            case "floz" -> amount * 29.5735;
            default -> null;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String round(double value) {
        return String.format("%.1f", value);
    }
}
