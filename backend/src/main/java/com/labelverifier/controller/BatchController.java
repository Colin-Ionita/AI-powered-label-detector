package com.labelverifier.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labelverifier.batch.BatchFile;
import com.labelverifier.batch.BatchService;
import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.BatchStartResponse;
import com.labelverifier.dto.BatchStatusResponse;
import com.labelverifier.dto.BatchVerificationResponse;
import com.labelverifier.exception.BadRequestException;
import com.labelverifier.service.FileValidationService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/batches")
public class BatchController {
    private final ObjectMapper objectMapper;
    private final FileValidationService fileValidationService;
    private final BatchService batchService;

    public BatchController(ObjectMapper objectMapper, FileValidationService fileValidationService, BatchService batchService) {
        this.objectMapper = objectMapper;
        this.fileValidationService = fileValidationService;
        this.batchService = batchService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchStartResponse startBatch(
        @RequestPart("images") MultipartFile[] images,
        @RequestPart("applicationData") String applicationDataJson
    ) throws IOException {
        ApplicationDataRequest applicationData = parseApplicationData(applicationDataJson);
        List<BatchFile> files = Arrays.stream(images)
            .peek(fileValidationService::validateImage)
            .map(this::toBatchFile)
            .toList();
        return batchService.start(files, applicationData);
    }

    @GetMapping("/{batchId}")
    public BatchStatusResponse getStatus(@PathVariable String batchId) {
        return batchService.status(batchId);
    }

    @GetMapping("/{batchId}/results")
    public BatchVerificationResponse getResults(@PathVariable String batchId) {
        return batchService.results(batchId);
    }

    private ApplicationDataRequest parseApplicationData(String applicationDataJson) {
        try {
            return objectMapper.readValue(applicationDataJson, ApplicationDataRequest.class);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Application data must be valid JSON.");
        }
    }

    private BatchFile toBatchFile(MultipartFile file) {
        try {
            return new BatchFile(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException ex) {
            throw new BadRequestException("Could not read uploaded file " + file.getOriginalFilename() + ".");
        }
    }
}
