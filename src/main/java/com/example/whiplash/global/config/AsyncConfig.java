package com.example.whiplash.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.whiplash.global.MdcTaskDecorator;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    // 퀴즈 전용 스레드 풀 설정값
    @Value("${async.quiz.core-pool-size:5}")
    private int quizCorePoolSize;

    @Value("${async.quiz.max-pool-size:10}")
    private int quizMaxPoolSize;

    @Value("${async.quiz.queue-capacity:100}")
    private int quizQueueCapacity;

    @Value("${async.quiz.thread-name-prefix:quiz-async-}")
    private String quizThreadNamePrefix;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 설정
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");

        // 1. 거부 정책 설정 (큐가 가득 찰 때)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 2. 우아한 종료 설정
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 3. MDC 컨텍스트 전파 (로깅 추적을 위해)
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * 퀴즈 생성 전용 Executor
     * @Async("quizTaskExecutor") 형태로 명시적 지정 시 사용
     */
    @Bean(name = "quizTaskExecutor")
    public Executor quizTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(quizCorePoolSize);
        executor.setMaxPoolSize(quizMaxPoolSize);
        executor.setQueueCapacity(quizQueueCapacity);
        executor.setThreadNamePrefix(quizThreadNamePrefix);

        // 기존 설정과 동일하게 적용
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // MDC 컨텍스트 전파 (기존과 동일)
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();

        log.info("퀴즈 전용 Executor 초기화: core={}, max={}, queue={}, prefix={}",
                quizCorePoolSize, quizMaxPoolSize, quizQueueCapacity, quizThreadNamePrefix);

        return executor;
    }

    /**
     * ⭐ 기사 보강 전용 Executor
     */
    @Bean(name = "articleEnrichmentExecutor")
    public Executor articleEnrichmentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 풀 설정
        executor.setCorePoolSize(2);              // 기본 스레드 수
        executor.setMaxPoolSize(5);               // 최대 스레드 수
        executor.setQueueCapacity(100);           // 큐 용량
        executor.setThreadNamePrefix("article-enrichment-");  // 스레드 이름

        // 스레드 풀이 가득 찼을 때 정책
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 애플리케이션 종료 시 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 초기화
        executor.initialize();

        log.info("✅ ArticleEnrichmentExecutor 초기화 완료: corePoolSize={}, maxPoolSize={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async method {} threw exception: {}",
                method.getName(),
                throwable.getMessage(),
                throwable);
        };
    }
}