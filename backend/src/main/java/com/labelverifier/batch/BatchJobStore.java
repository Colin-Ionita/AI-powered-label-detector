package com.labelverifier.batch;

import com.labelverifier.exception.NotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class BatchJobStore {
    private final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();

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
}
