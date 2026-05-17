package com.labelverifier.dto;

public record GovernmentWarningResult(
    MatchStatus status,
    int confidence,
    String extractedText,
    String reason
) {
}
