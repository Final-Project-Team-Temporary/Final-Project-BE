package com.example.whiplash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${async.quiz.core-pool-size:5}")
    private int corePoolSize;

    @Value("${async.quiz.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${async.quiz.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.quiz.thread-name-prefix:quiz-async-}")
    private String threadNamePrefix;

    @Bean(name = "quizTaskExecutor")
    public Executor quizTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
