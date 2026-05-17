package com.labelverifier.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.BeverageType;
import com.labelverifier.dto.VerificationStatus;
import com.labelverifier.extraction.RuleBasedFieldExtractionProvider;
import com.labelverifier.ocr.OcrResult;
import org.junit.jupiter.api.Test;

class VerificationServiceTest {
    private static final String CANONICAL_WARNING = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.";
    private static final String TRUNCATED_WARNING = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects.";

    @Test
    void truncatedWarningFailsInsteadOfPassingAsCanonical() {
        VerificationService service = serviceReturning(new OcrResult("test", bourbonText(TRUNCATED_WARNING), 96, true));

        var result = service.verify("truncated-warning.png", "image/png", new byte[] {1}, bourbonApplication());

        assertThat(result.overallStatus()).isEqualTo(VerificationStatus.FAIL);
        assertThat(result.governmentWarning().reason()).isEqualTo("BODY_TEXT_CHANGED");
    }

    @Test
    void partialBrandAndMissingWarningIsUnreadable() {
        String partialRead = """
            LD TOM
            Kentucky Straight Bourbon Whiskey
            45% Alc./Vol. (90 Proof)
            750 mL
            Bottled by Old Tom Distillery, Louisville, KY
            """;
        VerificationService service = serviceReturning(new OcrResult("test", partialRead, 97, false));

        var result = service.verify("blurry-label.png", "image/png", new byte[] {1}, bourbonApplication());

        assertThat(result.overallStatus()).isEqualTo(VerificationStatus.UNREADABLE);
    }

    private VerificationService serviceReturning(OcrResult ocrResult) {
        return new VerificationService(
            new OcrService((filename, imageBytes, contentType) -> ocrResult),
            new RuleBasedFieldExtractionProvider(),
            new FieldComparator(new NormalizationService()),
            new GovernmentWarningValidator(new NormalizationService(), false)
        );
    }

    private ApplicationDataRequest bourbonApplication() {
        return new ApplicationDataRequest(
            "COLA-DEMO-001",
            BeverageType.DISTILLED_SPIRITS,
            "Old Tom Distillery",
            "Kentucky Straight Bourbon Whiskey",
            "45% ABV (90 Proof)",
            "750 mL",
            "Old Tom Distillery",
            "Louisville, KY",
            "",
            false
        );
    }

    private String bourbonText(String warning) {
        return String.join("\n",
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            warning
        );
    }
}
