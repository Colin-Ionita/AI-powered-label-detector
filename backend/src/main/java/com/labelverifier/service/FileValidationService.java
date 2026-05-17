package com.labelverifier.service;

import com.labelverifier.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidationService {
    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

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
    }
}
