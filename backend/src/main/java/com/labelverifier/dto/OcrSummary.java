package com.labelverifier.dto;

public record OcrSummary(
    String provider,
    String fullText,
    int confidence,
    int wordCount,
    boolean headingBoldDetected
) {
}
