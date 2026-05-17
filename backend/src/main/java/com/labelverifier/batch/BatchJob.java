package com.labelverifier.batch;

import com.labelverifier.dto.VerificationResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchJob {
    private final String batchId;
    private final int total;
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger pass = new AtomicInteger();
    private final AtomicInteger fail = new AtomicInteger();
    private final AtomicInteger needsReview = new AtomicInteger();
    private final AtomicInteger unreadable = new AtomicInteger();
    private final List<VerificationResponse> results = new CopyOnWriteArrayList<>();
    private volatile boolean complete;

    public BatchJob(String batchId, int total) {
        this.batchId = batchId;
        this.total = total;
    }

    public void addResult(VerificationResponse result) {
        results.add(result);
        processed.incrementAndGet();
        switch (result.overallStatus()) {
            case PASS -> pass.incrementAndGet();
            case FAIL -> fail.incrementAndGet();
            case NEEDS_REVIEW -> needsReview.incrementAndGet();
            case UNREADABLE -> unreadable.incrementAndGet();
        }
        if (processed.get() >= total) {
            complete = true;
        }
    }

    public String batchId() {
        return batchId;
    }

    public int total() {
        return total;
    }

    public int processed() {
        return processed.get();
    }

    public int pass() {
        return pass.get();
    }

    public int fail() {
        return fail.get();
    }

    public int needsReview() {
        return needsReview.get();
    }

    public int unreadable() {
        return unreadable.get();
    }

    public boolean complete() {
        return complete;
    }

    public void markComplete() {
        complete = true;
    }

    public List<VerificationResponse> results() {
        return results.stream()
            .sorted((left, right) -> left.filename().compareToIgnoreCase(right.filename()))
            .toList();
    }
}
