package com.labelverifier.batch;

import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.BatchStartResponse;
import com.labelverifier.dto.BatchStatusResponse;
import com.labelverifier.dto.BatchVerificationResponse;
import com.labelverifier.dto.GovernmentWarningResult;
import com.labelverifier.dto.MatchStatus;
import com.labelverifier.dto.OcrSummary;
import com.labelverifier.dto.VerificationResponse;
import com.labelverifier.dto.VerificationStatus;
import com.labelverifier.exception.BadRequestException;
import com.labelverifier.service.VerificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BatchService {
    private static final Logger log = LoggerFactory.getLogger(BatchService.class);

    private final BatchJobStore jobStore;
    private final VerificationService verificationService;
    private final Executor batchExecutor;
    private final int maxBatchSize;

    public BatchService(
        BatchJobStore jobStore,
        VerificationService verificationService,
        @Qualifier("batchExecutor") Executor batchExecutor,
        @Value("${app.batch.max-size}") int maxBatchSize
    ) {
        this.jobStore = jobStore;
        this.verificationService = verificationService;
        this.batchExecutor = batchExecutor;
        this.maxBatchSize = maxBatchSize;
    }

    public BatchStartResponse start(List<BatchFile> files, ApplicationDataRequest applicationData) {
        if (files.isEmpty()) {
            throw new BadRequestException("Upload at least one label image for batch verification.");
        }
        if (files.size() > maxBatchSize) {
            throw new BadRequestException("Batch uploads are limited to " + maxBatchSize + " files.");
        }
        String batchId = UUID.randomUUID().toString();
        BatchJob job = jobStore.create(batchId, files.size());
        log.info("Starting batch {} with {} files", batchId, files.size());
        CompletableFuture.runAsync(() -> process(job, files, applicationData))
            .exceptionally(ex -> {
                log.error("Batch {} terminated unexpectedly", batchId, ex);
                job.markComplete();
                return null;
            });
        return new BatchStartResponse(batchId, files.size(), "PROCESSING");
    }

    public BatchStatusResponse status(String batchId) {
        return status(jobStore.get(batchId));
    }

    public BatchVerificationResponse results(String batchId) {
        BatchJob job = jobStore.get(batchId);
        return new BatchVerificationResponse(status(job), job.results());
    }

    private void process(BatchJob job, List<BatchFile> files, ApplicationDataRequest applicationData) {
        try {
            List<CompletableFuture<Void>> tasks = new ArrayList<>();
            for (BatchFile file : files) {
                try {
                    tasks.add(CompletableFuture.runAsync(() -> processFile(job, file, applicationData), batchExecutor));
                } catch (RuntimeException ex) {
                    log.error("Batch {} could not schedule verification for {}", job.batchId(), file.filename(), ex);
                    job.addResult(failureResult(file, ex));
                }
            }
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        } finally {
            job.markComplete();
            log.info("Completed batch {} with {}/{} files processed", job.batchId(), job.processed(), job.total());
        }
    }

    private void processFile(BatchJob job, BatchFile file, ApplicationDataRequest applicationData) {
        try {
            var result = verificationService.verify(file.filename(), file.contentType(), file.bytes(), applicationData);
            job.addResult(result);
        } catch (Exception ex) {
            log.error("Batch {} verification failed for {}", job.batchId(), file.filename(), ex);
            job.addResult(failureResult(file, ex));
        }
    }

    private VerificationResponse failureResult(BatchFile file, Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getClass().getSimpleName();
        }
        return new VerificationResponse(
            UUID.randomUUID().toString(),
            file.filename(),
            VerificationStatus.FAIL,
            0,
            List.of(),
            new GovernmentWarningResult(MatchStatus.MISSING, 0, null, "Verification failed before warning could be checked."),
            new OcrSummary("internal", "", 0, 0, false),
            List.of("Could not verify this image: " + message)
        );
    }

    private BatchStatusResponse status(BatchJob job) {
        return new BatchStatusResponse(
            job.batchId(),
            job.total(),
            job.processed(),
            job.pass(),
            job.fail(),
            job.needsReview(),
            job.unreadable(),
            job.complete()
        );
    }
}
