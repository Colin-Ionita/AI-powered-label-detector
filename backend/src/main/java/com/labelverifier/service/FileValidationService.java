package com.labelverifier.service;

import com.labelverifier.exception.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidationService {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private static final byte[] PNG_SIGNATURE = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Upload a JPEG, PNG, or WebP label image.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Each label image must be 10 MB or smaller.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp")) {
            throw new BadRequestException("Only JPEG, PNG, and WebP label images are supported.");
        }
        if (!contentMatchesType(file, contentType)) {
            throw new BadRequestException("File content does not match a supported JPEG, PNG, or WebP image.");
        }
    }

    private boolean contentMatchesType(MultipartFile file, String contentType) {
        byte[] header = readHeader(file);
        return switch (contentType) {
            case "image/png" -> hasPrefix(header, PNG_SIGNATURE);
            case "image/jpeg" -> header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
            case "image/webp" -> header.length >= 12
                && asciiEquals(header, 0, "RIFF")
                && asciiEquals(header, 8, "WEBP");
            default -> false;
        };
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.readNBytes(12);
        } catch (IOException ex) {
            throw new BadRequestException("Could not read uploaded label image.");
        }
    }

    private boolean hasPrefix(byte[] bytes, byte[] prefix) {
        return bytes.length >= prefix.length && Arrays.equals(Arrays.copyOf(bytes, prefix.length), prefix);
    }

    private boolean asciiEquals(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != (byte) expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }
}
