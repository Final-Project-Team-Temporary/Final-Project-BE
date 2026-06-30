package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.constant.QuizCacheConstants;
    import com.example.whiplash.quiz.document.TermQuizPool;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
     * 활성 사용자 기준 우선순위 용어를 선별해 퀴즈 풀을 생성한다.
     *
     * 1. 활성 사용자 보유 용어를 유저 수 기준 내림차순으로 조회 (MySQL 1회)
     * 2. MongoDB에 이미 있는 termName을 bulk 조회 (MongoDB 1회)
     * 3. 미생성 용어를 quizTaskExecutor 에서 병렬 생성 + 실패 시 Redis Set 기록
     */
    @Transactional(readOnly = true)
    public void generateQuizzesForAllTerms() {
        long startTime = System.currentTimeMillis();
        log.info("=== 퀴즈 배치 생성 시작 ===");

        // 1. 활성 사용자 기준 우선순위 용어 조회 — 여유분(×2)까지 요청해 필터 후에도 maxTermsPerBatch 확보
        LocalDateTime activeThreshold = LocalDateTime.now().minusDays(activeDaysThreshold);
        List<String> prioritizedTerms = userTermsRepository.findPrioritizedTermNames(
                activeThreshold, PageRequest.of(0, maxTermsPerBatch * 2));

        log.info("활성 사용자 보유 용어 수: {}", prioritizedTerms.size());

        if (prioritizedTerms.isEmpty()) {
            log.info("처리할 용어 없음.");
            return;
        }

        // 2. MongoDB 존재 용어 bulk 1회 조회 → Set으로 변환
        Set<String> existingTerms = termQuizPoolRepository.findAllWithTermNameOnly()
                .stream()
                .map(TermQuizPool::getTermName)
                .collect(Collectors.toSet());

        // 3. 미생성 용어 필터링
        List<String> termsWithoutPool = prioritizedTerms.stream()
                .filter(term -> !existingTerms.contains(term))
                .limit(maxTermsPerBatch)
                .toList();

        log.info("풀 미생성 용어 수: {} / 조회된 용어 {}", termsWithoutPool.size(), prioritizedTerms.size());

        if (termsWithoutPool.isEmpty()) {
            log.info("모든 용어에 퀴즈 풀 존재. 배치 종료.");
            return;
        }

        // 4. 병렬 생성 — Semaphore 로 AI 서버 동시 호출 수 제한
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
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
                    // 실패 용어를 Redis Set에 기록 → 복구 잡이 재시도
                    redisTemplate.opsForSet().add(QuizCacheConstants.BATCH_FAILED_SET_KEY, term);
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
     * Redis 실패 Set에 기록된 용어를 재시도한다.
     * 성공하면 Set에서 제거하고, 실패하면 Set에 남겨 다음 복구 잡에서 재시도한다.
     */
    public void retryFailedTerms() {
        Set<Object> failedTerms = redisTemplate.opsForSet()
                .members(QuizCacheConstants.BATCH_FAILED_SET_KEY);

        if (failedTerms == null || failedTerms.isEmpty()) {
            log.debug("재시도할 실패 용어 없음.");
            return;
        }

        log.info("실패 용어 재시도 시작: count={}", failedTerms.size());

        for (Object termObj : failedTerms) {
            String term = (String) termObj;
            try {
                String cacheKey = QuizCacheConstants.poolKey(term);
                termQuizPoolService.generateAndPersist(term, cacheKey);
                redisTemplate.opsForSet().remove(QuizCacheConstants.BATCH_FAILED_SET_KEY, term);
                log.info("실패 용어 재시도 성공: term={}", term);
            } catch (Exception e) {
                log.error("실패 용어 재시도 실패 (다음 복구 잡에서 재시도): term={}", term, e);
            }
        }
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
