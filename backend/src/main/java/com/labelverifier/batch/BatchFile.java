package com.labelverifier.batch;

public record BatchFile(
    String filename,
    String contentType,
    byte[] bytes
) {
}
