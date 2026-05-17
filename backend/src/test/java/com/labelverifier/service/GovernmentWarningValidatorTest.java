package com.labelverifier.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.labelverifier.dto.MatchStatus;
import org.junit.jupiter.api.Test;

class GovernmentWarningValidatorTest {
    private static final String CANONICAL = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.";

    private final GovernmentWarningValidator demoValidator = new GovernmentWarningValidator(new NormalizationService(), false);
    private final GovernmentWarningValidator strictValidator = new GovernmentWarningValidator(new NormalizationService(), true);

    @Test
    void exactWarningWithKnownBoldPasses() {
        var result = demoValidator.validate(CANONICAL, 95, true);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCH);
    }

    @Test
    void titleCaseHeadingFails() {
        var result = demoValidator.validate(CANONICAL.replace("GOVERNMENT WARNING:", "Government Warning:"), 95, false);

        assertThat(result.status()).isEqualTo(MatchStatus.MISMATCH);
        assertThat(result.reason()).isEqualTo("HEADING_CASE");
    }

    @Test
    void truncatedWarningFailsEvenWhenItStartsCorrectly() {
        var truncated = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects.";

        var result = demoValidator.validate(truncated, 95, true);

        assertThat(result.status()).isEqualTo(MatchStatus.MISMATCH);
        assertThat(result.reason()).isEqualTo("BODY_TEXT_CHANGED");
    }

    @Test
    void unverifiableBoldNeedsReview() {
        var result = strictValidator.validate(CANONICAL, 95, false);

        assertThat(result.status()).isEqualTo(MatchStatus.LOW_CONFIDENCE);
        assertThat(result.reason()).isEqualTo("BOLD_UNVERIFIED");
    }

    @Test
    void unverifiableBoldCanPassWhenStrictMetadataIsDisabled() {
        var result = demoValidator.validate(CANONICAL, 95, false);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCH);
    }
}
