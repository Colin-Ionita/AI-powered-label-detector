package com.labelverifier.batch;

import com.labelverifier.exception.NotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchJobStore {
    private final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();
    private final long retentionMs;

    public BatchJobStore(@Value("${app.batch.retention-ms}") long retentionMs) {
        this.retentionMs = retentionMs;
    }

    public BatchJob create(String batchId, int total) {
        BatchJob job = new BatchJob(batchId, total);
        jobs.put(batchId, job);
        return job;
    }

    public BatchJob get(String batchId) {
        BatchJob job = jobs.get(batchId);
        if (job == null) {
            throw new NotFoundException("Batch job was not found.");
        }
        return job;
    }

    @Scheduled(fixedRateString = "${app.batch.cleanup-rate-ms}")
    public void evictStale() {
        long cutoff = System.currentTimeMillis() - retentionMs;
        jobs.entrySet().removeIf(entry -> entry.getValue().createdAtMs() < cutoff);
    }
}
