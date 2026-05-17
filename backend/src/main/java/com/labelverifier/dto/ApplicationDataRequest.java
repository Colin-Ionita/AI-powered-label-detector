package com.labelverifier.dto;

public record ApplicationDataRequest(
    String applicationId,
    BeverageType beverageType,
    String brandName,
    String classOrType,
    String alcoholContent,
    String netContents,
    String responsiblePartyName,
    String responsiblePartyAddress,
    String countryOfOrigin,
    boolean imported
) {
}
