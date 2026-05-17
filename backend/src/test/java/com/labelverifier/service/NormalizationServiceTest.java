package com.labelverifier.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NormalizationServiceTest {
    private final NormalizationService normalizationService = new NormalizationService();

    @Test
    void handlesCasingAndApostropheDifferences() {
        assertThat(normalizationService.similarity("Stone's Throw", "STONE'S THROW")).isGreaterThan(0.95);
    }

    @Test
    void handlesAmpersandEquivalence() {
        assertThat(normalizationService.similarity("Smith & Sons", "Smith and Sons")).isGreaterThan(0.95);
    }
}
