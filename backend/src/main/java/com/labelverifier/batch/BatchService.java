package com.labelverifier.batch;

import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.BatchStartResponse;
import com.labelverifier.dto.BatchStatusResponse;
import com.labelverifier.dto.BatchVerificationResponse;
import com.labelverifier.exception.BadRequestException;
import com.labelverifier.service.VerificationService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BatchService {
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
        CompletableFuture.runAsync(() -> process(job, files, applicationData), batchExecutor);
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
            List<CompletableFuture<Void>> tasks = files.stream()
                .map(file -> CompletableFuture.runAsync(() -> {
                    var result = verificationService.verify(file.filename(), file.contentType(), file.bytes(), applicationData);
                    job.addResult(result);
                }, batchExecutor))
                .toList();
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        } finally {
            job.markComplete();
        }
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
