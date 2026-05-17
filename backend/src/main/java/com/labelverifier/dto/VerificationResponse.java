package com.labelverifier.dto;

import java.util.List;

public record VerificationResponse(
    String labelId,
    String filename,
    VerificationStatus overallStatus,
    long processingTimeMs,
    List<FieldMatchResult> fields,
    GovernmentWarningResult governmentWarning,
    OcrSummary ocr,
    List<String> reviewReasons
) {
}
