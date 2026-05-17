package com.labelverifier.ocr;

public record OcrResult(
    String provider,
    String fullText,
    int confidence,
    boolean warningHeadingBoldDetected
) {
    public int wordCount() {
        if (fullText == null || fullText.isBlank()) {
            return 0;
        }
        return fullText.trim().split("\\s+").length;
    }
}
