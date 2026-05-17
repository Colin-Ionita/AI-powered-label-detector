package com.labelverifier.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.labelverifier.dto.MatchStatus;
import org.junit.jupiter.api.Test;

class FieldComparatorTest {
    private final FieldComparator comparator = new FieldComparator(new NormalizationService());

    @Test
    void alcoholContentMatchesEquivalentAbvAndProof() {
        var result = comparator.alcohol("45% ABV", "45% Alc./Vol. (90 Proof)", 96);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCH);
    }

    @Test
    void alcoholContentFailsOutsideTolerance() {
        var result = comparator.alcohol("45% ABV", "42% Alc./Vol. (84 Proof)", 96);

        assertThat(result.status()).isEqualTo(MatchStatus.MISMATCH);
    }

    @Test
    void netContentsNormalizeLitersAndMilliliters() {
        var result = comparator.netContents("750 mL", "0.75 L", 96);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCH);
    }
}
