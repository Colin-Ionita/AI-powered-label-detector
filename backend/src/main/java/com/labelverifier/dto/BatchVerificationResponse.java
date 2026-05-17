package com.labelverifier.dto;

import java.util.List;

public record BatchVerificationResponse(
    BatchStatusResponse status,
    List<VerificationResponse> results
) {
}
