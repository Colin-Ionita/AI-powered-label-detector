package com.labelverifier.exception;

public class OcrProviderException extends RuntimeException {
    public OcrProviderException(String message) {
        super(message);
    }

    public OcrProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
