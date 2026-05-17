package com.labelverifier.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.labelverifier.dto.ApplicationDataRequest;
import com.labelverifier.dto.BeverageType;
import com.labelverifier.dto.VerificationResponse;
import com.labelverifier.dto.VerificationStatus;
import com.labelverifier.service.VerificationService;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class BatchServiceTest {
    @Test
    void failedFileStillProducesBatchResult() throws InterruptedException {
        Executor directExecutor = Runnable::run;
        BatchService service = new BatchService(new BatchJobStore(60_000), new ThrowingVerificationService(), directExecutor, 10);

        var started = service.start(
            List.of(new BatchFile("bad-label.png", "image/png", new byte[] {1, 2, 3})),
            applicationData()
        );
        var results = awaitResults(service, started.batchId());

        assertThat(results.status().complete()).isTrue();
        assertThat(results.status().processed()).isEqualTo(1);
        assertThat(results.status().fail()).isEqualTo(1);
        assertThat(results.results()).hasSize(1);
        assertThat(results.results().getFirst().overallStatus()).isEqualTo(VerificationStatus.FAIL);
        assertThat(results.results().getFirst().reviewReasons()).contains("Could not verify this image: OCR provider unavailable");
    }

    private static class ThrowingVerificationService extends VerificationService {
        ThrowingVerificationService() {
            super(null, null, null, null);
        }

        @Override
        public VerificationResponse verify(String filename, String contentType, byte[] bytes, ApplicationDataRequest applicationData) {
            throw new RuntimeException("OCR provider unavailable");
        }
    }

    private com.labelverifier.dto.BatchVerificationResponse awaitResults(BatchService service, String batchId) throws InterruptedException {
        for (int attempt = 0; attempt < 20; attempt++) {
            var results = service.results(batchId);
            if (results.status().complete()) {
                return results;
            }
            Thread.sleep(25);
        }
        return service.results(batchId);
    }

    private ApplicationDataRequest applicationData() {
        return new ApplicationDataRequest(
            "COLA-DEMO-001",
            BeverageType.DISTILLED_SPIRITS,
            "Old Tom Distillery",
            "Kentucky Straight Bourbon Whiskey",
            "45% ABV",
            "750 mL",
            "Old Tom Distillery",
            "Louisville, KY",
            "",
            false
        );
    }
}
