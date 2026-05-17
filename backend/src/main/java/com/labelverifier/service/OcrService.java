package com.labelverifier.service;

import com.labelverifier.ocr.OcrProvider;
import com.labelverifier.ocr.OcrResult;
import org.springframework.stereotype.Service;

@Service
public class OcrService {
    private final OcrProvider ocrProvider;

    public OcrService(OcrProvider ocrProvider) {
        this.ocrProvider = ocrProvider;
    }

    public OcrResult readText(String filename, byte[] imageBytes, String contentType) {
        return ocrProvider.readText(filename, imageBytes, contentType);
    }
}
