package com.labelverifier.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean(name = "batchExecutor")
    public Executor batchExecutor(@Value("${app.batch.worker-concurrency}") int concurrency) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(Math.max(concurrency, concurrency * 2));
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-verifier-");
        executor.initialize();
        return executor;
    }
}
