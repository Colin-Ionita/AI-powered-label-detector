package com.labelverifier.service;

import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.ExtractedLabelFields;
import com.labelverifier.dto.FieldMatchResult;
import com.labelverifier.dto.GovernmentWarningResult;
import com.labelverifier.dto.MatchStatus;
import com.labelverifier.dto.OcrSummary;
import com.labelverifier.dto.VerificationResponse;
import com.labelverifier.dto.VerificationStatus;
import com.labelverifier.extraction.FieldExtractionProvider;
import com.labelverifier.ocr.OcrResult;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {
    private final OcrService ocrService;
    private final FieldExtractionProvider extractionProvider;
    private final FieldComparator fieldComparator;
    private final GovernmentWarningValidator governmentWarningValidator;

    public VerificationService(
        OcrService ocrService,
        FieldExtractionProvider extractionProvider,
        FieldComparator fieldComparator,
        GovernmentWarningValidator governmentWarningValidator
    ) {
        this.ocrService = ocrService;
        this.extractionProvider = extractionProvider;
        this.fieldComparator = fieldComparator;
        this.governmentWarningValidator = governmentWarningValidator;
    }

    public VerificationResponse verify(String filename, String contentType, byte[] bytes, ApplicationDataRequest applicationData) {
        Instant start = Instant.now();
        OcrResult ocr = ocrService.readText(filename, bytes, contentType);
        OcrSummary ocrSummary = new OcrSummary(
            ocr.provider(),
            ocr.fullText(),
            ocr.confidence(),
            ocr.wordCount(),
            ocr.warningHeadingBoldDetected()
        );

        if (ocr.confidence() < 35 || ocr.wordCount() < 8) {
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            return new VerificationResponse(
                UUID.randomUUID().toString(),
                filename,
                VerificationStatus.UNREADABLE,
                elapsed,
                List.of(),
                new GovernmentWarningResult(MatchStatus.MISSING, 0, null, "Image text was not readable enough to verify."),
                ocrSummary,
                List.of("Image text could not be read clearly. Try a higher-resolution image or flatter angle.")
            );
        }

        ExtractedLabelFields extracted = extractionProvider.extract(ocr);
        List<FieldMatchResult> fields = new ArrayList<>();
        fields.add(fieldComparator.text("brandName", "Brand name", applicationData.brandName(), extracted.brandName(), extracted.brandNameConfidence(), 0.86));
        fields.add(fieldComparator.text("classOrType", "Class/type", applicationData.classOrType(), extracted.classOrType(), extracted.classOrTypeConfidence(), 0.72));
        fields.add(fieldComparator.alcohol(applicationData.alcoholContent(), extracted.alcoholContent(), extracted.alcoholContentConfidence()));
        fields.add(fieldComparator.netContents(applicationData.netContents(), extracted.netContents(), extracted.netContentsConfidence()));
        fields.add(fieldComparator.text("responsiblePartyName", "Responsible party", applicationData.responsiblePartyName(), extracted.responsiblePartyName(), extracted.responsiblePartyConfidence(), 0.75));
        fields.add(fieldComparator.text("responsiblePartyAddress", "Address", applicationData.responsiblePartyAddress(), extracted.responsiblePartyAddress(), extracted.responsiblePartyConfidence(), 0.60));
        if (applicationData.imported()) {
            fields.add(fieldComparator.text("countryOfOrigin", "Country of origin", applicationData.countryOfOrigin(), extracted.countryOfOrigin(), extracted.countryOfOriginConfidence(), 0.88));
        } else {
            fields.add(new FieldMatchResult("countryOfOrigin", "Country of origin", applicationData.countryOfOrigin(), extracted.countryOfOrigin(), MatchStatus.NOT_APPLICABLE, 100, "Not required for domestic products."));
        }

        GovernmentWarningResult warning = governmentWarningValidator.validate(
            extracted.governmentWarning(),
            extracted.governmentWarningConfidence(),
            ocr.warningHeadingBoldDetected()
        );

        List<String> reviewReasons = reviewReasons(fields, warning, ocr);
        VerificationStatus overallStatus = overallStatus(fields, warning, ocr);
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        return new VerificationResponse(UUID.randomUUID().toString(), filename, overallStatus, elapsed, fields, warning, ocrSummary, reviewReasons);
    }

    private VerificationStatus overallStatus(List<FieldMatchResult> fields, GovernmentWarningResult warning, OcrResult ocr) {
        if (ocr.confidence() < 50) {
            return VerificationStatus.NEEDS_REVIEW;
        }
        boolean hasFailure = fields.stream().anyMatch(field -> field.status() == MatchStatus.MISMATCH || field.status() == MatchStatus.MISSING)
            || warning.status() == MatchStatus.MISMATCH
            || warning.status() == MatchStatus.MISSING;
        if (hasFailure) {
            return VerificationStatus.FAIL;
        }
        boolean needsReview = fields.stream().anyMatch(field -> field.status() == MatchStatus.LOW_CONFIDENCE)
            || warning.status() == MatchStatus.LOW_CONFIDENCE;
        return needsReview ? VerificationStatus.NEEDS_REVIEW : VerificationStatus.PASS;
    }

    private List<String> reviewReasons(List<FieldMatchResult> fields, GovernmentWarningResult warning, OcrResult ocr) {
        List<String> reasons = new ArrayList<>();
        if (ocr.confidence() < 70) {
            reasons.add("OCR confidence is low.");
        }
        for (FieldMatchResult field : fields) {
            if (field.status() == MatchStatus.MISMATCH || field.status() == MatchStatus.MISSING || field.status() == MatchStatus.LOW_CONFIDENCE) {
                reasons.add(field.displayName() + ": " + field.reason());
            }
        }
        if (warning.status() != MatchStatus.MATCH) {
            reasons.add("Government warning: " + warning.reason());
        }
        return reasons;
    }
}
