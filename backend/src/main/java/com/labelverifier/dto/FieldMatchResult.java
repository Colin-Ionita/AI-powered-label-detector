package com.labelverifier.dto;

public record FieldMatchResult(
    String fieldKey,
    String displayName,
    String expectedValue,
    String extractedValue,
    MatchStatus status,
    int confidence,
    String reason
) {
}
