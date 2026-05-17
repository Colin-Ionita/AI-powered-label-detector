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

    @Test
    void partialBrandTextDoesNotMatchFullBrand() {
        var result = comparator.text("brandName", "Brand name", "Old Tom Distillery", "LD TOM", 96, 0.86);

        assertThat(result.status()).isEqualTo(MatchStatus.MISMATCH);
    }

    @Test
    void addressCanMatchInsideResponsiblePartyLine() {
        var result = comparator.text("responsiblePartyAddress", "Address", "Louisville, KY", "Bottled by Old Tom Distillery, Louisville, KY", 96, 0.60);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCH);
    }
}
