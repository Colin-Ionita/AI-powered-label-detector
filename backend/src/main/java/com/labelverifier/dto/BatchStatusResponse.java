package com.labelverifier.dto;

public record BatchStatusResponse(
    String batchId,
    int total,
    int processed,
    int pass,
    int fail,
    int needsReview,
    int unreadable,
    boolean complete
) {
}
