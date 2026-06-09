package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizBatchService {

    private final UserRepository userRepository;
    private final UserTermsRepository userTermsRepository;
    private final TermQuizPoolRepository termQuizPoolRepository;
    private final TermQuizPoolService termQuizPoolService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("quizTaskExecutor")
    private final Executor quizTaskExecutor;

    @Value("${quiz.batch.active-days-threshold:30}")
    private int activeDaysThreshold;

    @Value("${quiz.batch.max-terms-per-batch:50}")
    private int maxTermsPerBatch;

    /**
     * 고유 용어 단위로 퀴즈 풀을 생성한다.
     *
     * 풀이 없는 용어들을 quizTaskExecutor 에서 병렬 처리하되,
     * Semaphore 로 AI 서버 동시 호출 수를 제한한다 (기본 5개).
     */
    @Transactional(readOnly = true)
    public void generateQuizzesForAllTerms() {
        long startTime = System.currentTimeMillis();
        log.info("=== 퀴즈 배치 생성 시작 (용어 단위) ===");

        // 1. 전체 사용자에 걸쳐 고유 termName 수집
        List<String> allDistinctTerms = userTermsRepository.findDistinctTermNames();
        log.info("전체 고유 용어 수: {}", allDistinctTerms.size());

        if (allDistinctTerms.isEmpty()) {
            log.info("처리할 용어 없음.");
            return;
        }

        // 2. MongoDB 풀이 없는 용어만 필터링 — 읽기는 트랜잭션 안에서 처리
        List<String> termsWithoutPool = allDistinctTerms.stream()
                .filter(term -> !termQuizPoolRepository.existsByTermName(term))
                .limit(maxTermsPerBatch)
                .toList();

        log.info("풀 미생성 용어 수: {} / 전체 {}", termsWithoutPool.size(), allDistinctTerms.size());

        if (termsWithoutPool.isEmpty()) {
            log.info("모든 용어에 퀴즈 풀 존재. 배치 종료.");
            return;
        }

        // 3. 병렬 생성 — Semaphore 로 AI 서버 동시 호출 수 제한
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        // quizTaskExecutor core-pool-size(기본 5)와 맞춰 동시 AI 호출도 5개로 제한
        Semaphore semaphore = new Semaphore(5);

        List<CompletableFuture<Void>> futures = new ArrayList<>(termsWithoutPool.size());

        for (String term : termsWithoutPool) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        String cacheKey = QuizCacheConstants.poolKey(term);
                        termQuizPoolService.generateAndPersist(term, cacheKey);
                        successCount.incrementAndGet();
                        log.debug("풀 생성 완료: term={}", term);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("배치 스레드 인터럽트: term={}", term);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("풀 생성 실패: term={}", term, e);
                }
            }, quizTaskExecutor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("=== 퀴즈 배치 완료: 성공={}, 실패={}, 소요={}ms ===",
                successCount.get(), failCount.get(), elapsed);
    }

    /**
     * 캐시 통계 조회 (모니터링용)
     */
    public CacheStatistics getCacheStatistics() {
        long totalTermsInMongo = termQuizPoolRepository.count();
        long totalDistinctTerms = userTermsRepository.findDistinctTermNames().size();

        var redisKeys = redisTemplate.keys(QuizCacheConstants.POOL_KEY_PREFIX + "*");
        int redisHotTerms = redisKeys != null ? redisKeys.size() : 0;

        LocalDateTime activeThreshold = LocalDateTime.now().minusDays(activeDaysThreshold);
        long activeUserCount = userRepository.countActiveUsersSince(activeThreshold);

        double coverageRate = totalDistinctTerms > 0
                ? (double) totalTermsInMongo / totalDistinctTerms * 100
                : 0;

        return new CacheStatistics(
                (int) totalTermsInMongo,
                redisHotTerms,
                activeUserCount,
                totalDistinctTerms,
                coverageRate
        );
    }

    @Data
    @AllArgsConstructor
    public static class CacheStatistics {
        private int totalTermsInMongo;
        private int redisHotTerms;
        private long activeUserCount;
        private long totalDistinctTerms;
        private double coverageRate;
    }
}
