package com.labelverifier.dto;

public record BatchStartResponse(
    String batchId,
    int total,
    String status
) {
}
