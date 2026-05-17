package com.labelverifier.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.VerificationResponse;
import com.labelverifier.exception.BadRequestException;
import com.labelverifier.service.FileValidationService;
import com.labelverifier.service.VerificationService;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {
    private final ObjectMapper objectMapper;
    private final FileValidationService fileValidationService;
    private final VerificationService verificationService;

    public VerificationController(
        ObjectMapper objectMapper,
        FileValidationService fileValidationService,
        VerificationService verificationService
    ) {
        this.objectMapper = objectMapper;
        this.fileValidationService = fileValidationService;
        this.verificationService = verificationService;
    }

    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VerificationResponse verifySingle(
        @RequestPart("image") MultipartFile image,
        @RequestPart("applicationData") String applicationDataJson
    ) throws IOException {
        fileValidationService.validateImage(image);
        ApplicationDataRequest applicationData = parseApplicationData(applicationDataJson);
        validateApplicationData(applicationData);
        return verificationService.verify(
            image.getOriginalFilename(),
            image.getContentType(),
            image.getBytes(),
            applicationData
        );
    }

    private ApplicationDataRequest parseApplicationData(String applicationDataJson) {
        try {
            return objectMapper.readValue(applicationDataJson, ApplicationDataRequest.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Application data must be valid JSON.");
        }
    }

    private void validateApplicationData(ApplicationDataRequest request) {
        if (isBlank(request.brandName()) || isBlank(request.alcoholContent())) {
            throw new BadRequestException("Brand name and alcohol content are required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
