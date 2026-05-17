package com.labelverifier.dto;

public record ExtractedLabelFields(
    String brandName,
    int brandNameConfidence,
    String classOrType,
    int classOrTypeConfidence,
    String alcoholContent,
    int alcoholContentConfidence,
    String netContents,
    int netContentsConfidence,
    String responsiblePartyName,
    int responsiblePartyConfidence,
    String responsiblePartyAddress,
    String countryOfOrigin,
    int countryOfOriginConfidence,
    String governmentWarning,
    int governmentWarningConfidence
) {
}
