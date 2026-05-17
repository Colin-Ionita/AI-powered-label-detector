package com.labelverifier.ocr;

public interface OcrProvider {
    OcrResult readText(String filename, byte[] imageBytes, String contentType);
}
