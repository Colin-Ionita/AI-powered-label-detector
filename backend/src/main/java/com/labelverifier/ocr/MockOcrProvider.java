package com.labelverifier.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ocr.provider", havingValue = "mock", matchIfMissing = true)
public class MockOcrProvider implements OcrProvider {
    private static final String CANONICAL_WARNING = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.";
    private static final String TRUNCATED_WARNING = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects.";

    @Override
    public OcrResult readText(String filename, byte[] imageBytes, String contentType) {
        String lowerName = filename == null ? "" : filename.toLowerCase();
        if (lowerName.contains("blur") || lowerName.contains("unreadable") || lowerName.contains("glare")) {
            return new OcrResult("mock", "OL T M D... unreadable glare low contrast", 28, false);
        }
        if (lowerName.contains("titlecase") || lowerName.contains("warning-fail")) {
            return new OcrResult("mock", bourbonLabel("OLD TOM DISTILLERY", "45% Alc./Vol. (90 Proof)", "750 mL", CANONICAL_WARNING)
                .replace("GOVERNMENT WARNING:", "Government Warning:"), 91, false);
        }
        if (lowerName.contains("wrong-abv")) {
            return new OcrResult("mock", bourbonLabel("OLD TOM DISTILLERY", "42% Alc./Vol. (84 Proof)", "750 mL", CANONICAL_WARNING), 94, true);
        }
        if (lowerName.contains("missing-net")) {
            return new OcrResult("mock", bourbonLabel("OLD TOM DISTILLERY", "45% Alc./Vol. (90 Proof)", "", CANONICAL_WARNING), 89, true);
        }
        if (lowerName.contains("truncated-warning")) {
            return new OcrResult("mock", bourbonLabel("OLD TOM DISTILLERY", "45% Alc./Vol. (90 Proof)", "750 mL", TRUNCATED_WARNING), 92, true);
        }
        if (lowerName.contains("stone")) {
            return new OcrResult("mock", stoneLabel(), 96, true);
        }
        if (lowerName.contains("import")) {
            return new OcrResult("mock", importLabel(), 93, true);
        }
        return new OcrResult("mock", bourbonLabel("OLD TOM DISTILLERY", "45% Alc./Vol. (90 Proof)", "750 mL", CANONICAL_WARNING), 96, true);
    }

    private String bourbonLabel(String brand, String alcohol, String netContents, String warning) {
        StringBuilder text = new StringBuilder();
        text.append(brand).append('\n');
        text.append("Kentucky Straight Bourbon Whiskey\n");
        if (!alcohol.isBlank()) {
            text.append(alcohol).append('\n');
        }
        if (!netContents.isBlank()) {
            text.append(netContents).append('\n');
        }
        text.append("Bottled by Old Tom Distillery, Louisville, KY\n");
        text.append(warning);
        return text.toString();
    }

    private String stoneLabel() {
        StringBuilder text = new StringBuilder();
        text.append("STONE'S THROW\n");
        text.append("Straight Bourbon Whiskey\n");
        text.append("40% Alc./Vol. (80 Proof)\n");
        text.append("750 mL\n");
        text.append("Bottled by Stone's Throw Distilling, Richmond, VA\n");
        text.append(CANONICAL_WARNING);
        return text.toString();
    }

    private String importLabel() {
        StringBuilder text = new StringBuilder();
        text.append("OLD TOM DISTILLERY\n");
        text.append("Canadian Whisky\n");
        text.append("45% Alc./Vol. (90 Proof)\n");
        text.append("750 mL\n");
        text.append("Product of Canada\n");
        text.append("Imported by Old Tom Imports, Buffalo, NY\n");
        text.append(CANONICAL_WARNING);
        return text.toString();
    }
}
