package com.labelverifier.extraction;

import com.labelverifier.dto.ExtractedLabelFields;
import com.labelverifier.ocr.OcrResult;

public interface FieldExtractionProvider {
    ExtractedLabelFields extract(OcrResult ocrResult);
}
