package com.labelverifier.extraction;

import com.labelverifier.dto.ExtractedLabelFields;
import com.labelverifier.ocr.OcrResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedFieldExtractionProvider implements FieldExtractionProvider {
    private static final Pattern ABV_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%\\s*(?:ABV|ALC\\.?/?VOL\\.?)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROOF_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*proof", Pattern.CASE_INSENSITIVE);
    private static final Pattern NET_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(mL|ml|ML|L|l|fl\\.?\\s*oz)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("(?:Product of|Country of Origin:)\\s*([A-Za-z ]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public ExtractedLabelFields extract(OcrResult ocrResult) {
        String text = ocrResult.fullText() == null ? "" : ocrResult.fullText();
        String[] lines = text.lines().map(String::trim).filter(line -> !line.isBlank()).toArray(String[]::new);
        String brand = lines.length > 0 ? lines[0] : null;
        String type = findLineContaining(lines, "whiskey", "whisky", "wine", "lager", "ale", "bourbon", "vodka", "rum");
        String alcohol = firstMatch(text, ABV_PATTERN);
        if (alcohol == null) {
            alcohol = firstMatch(text, PROOF_PATTERN);
        }
        String net = firstMatch(text, NET_PATTERN);
        String partyLine = findLineContaining(lines, "bottled by", "produced by", "imported by");
        String country = group(text, COUNTRY_PATTERN, 1);
        String warning = extractWarning(text);
        int baseConfidence = Math.max(0, Math.min(100, ocrResult.confidence()));

        return new ExtractedLabelFields(
            brand,
            fieldConfidence(baseConfidence, brand),
            type,
            fieldConfidence(baseConfidence, type),
            alcohol,
            fieldConfidence(baseConfidence, alcohol),
            net,
            fieldConfidence(baseConfidence, net),
            extractResponsibleName(partyLine),
            fieldConfidence(baseConfidence, partyLine),
            partyLine,
            country,
            fieldConfidence(baseConfidence, country),
            warning,
            fieldConfidence(baseConfidence, warning)
        );
    }

    private int fieldConfidence(int base, String value) {
        return value == null || value.isBlank() ? 0 : base;
    }

    private String findLineContaining(String[] lines, String... terms) {
        for (String line : lines) {
            String lower = line.toLowerCase();
            for (String term : terms) {
                if (lower.contains(term)) {
                    return line;
                }
            }
        }
        return null;
    }

    private String firstMatch(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String group(String text, Pattern pattern, int group) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group).trim() : null;
    }

    private String extractWarning(String text) {
        int index = text.toLowerCase().indexOf("government warning");
        if (index < 0) {
            return null;
        }
        return text.substring(index).trim();
    }

    private String extractResponsibleName(String partyLine) {
        if (partyLine == null) {
            return null;
        }
        return partyLine
            .replaceFirst("(?i)bottled by\\s+", "")
            .replaceFirst("(?i)produced by\\s+", "")
            .replaceFirst("(?i)imported by\\s+", "")
            .split(",")[0]
            .trim();
    }
}
