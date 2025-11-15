package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizBatchService {

    private final UserRepository userRepository;
    private final UserTermsRepository userTermsRepository;
    private final AiServerClient aiServerClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${quiz.batch.active-days-threshold:30}")
    private int activeDaysThreshold;

    @Value("${quiz.batch.max-terms-per-user:10}")
    private int maxTermsPerUser;

    private static final String CACHE_KEY_PREFIX = "quiz:single:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    /**
     * 모든 활성 사용자에 대해 퀴즈 미리 생성
     */
    @Transactional(readOnly = true)
    public void generateQuizzesForAllActiveUsers() {
        long startTime = System.currentTimeMillis();
        log.info("=== 퀴즈 배치 생성 시작 ===");

        // 1. 활성 사용자 조회
        LocalDateTime activeThreshold = LocalDateTime.now().minusDays(activeDaysThreshold);
        List<User> activeUsers = userRepository.findActiveUsersSince(activeThreshold);

        log.info("활성 사용자 수: {}", activeUsers.size());

        if (activeUsers.isEmpty()) {
            log.info("활성 사용자가 없습니다.");
            return;
        }

        // 2. 각 사용자별 퀴즈 생성
        int totalGenerated = 0;
        int successCount = 0;
        int failCount = 0;

        for (User user : activeUsers) {
            try {
                int generated = generateQuizzesForUser(user);
                totalGenerated += generated;
                successCount++;

                log.info("사용자 퀴즈 생성 완료: userId={}, generated={}",
                        user.getId(), generated);

            } catch (Exception e) {
                failCount++;
                log.error("사용자 퀴즈 생성 실패: userId={}", user.getId(), e);
            }
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("=== 퀴즈 배치 생성 완료 ===");
        log.info("처리 사용자: {}, 성공: {}, 실패: {}",
                activeUsers.size(), successCount, failCount);
        log.info("생성된 퀴즈 총 개수: {}", totalGenerated);
        log.info("소요 시간: {}ms", elapsedTime);
    }

    /**
     * 특정 사용자에 대해 퀴즈 생성
     */
    private int generateQuizzesForUser(User user) {
        Long userId = user.getId();

        // 1. 사용자가 저장한 모든 용어 조회
        List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);

        if (userTermsList.isEmpty()) {
            log.debug("저장된 용어 없음: userId={}", userId);
            return 0;
        }

        // 2. 캐시 없는 용어만 필터링
        List<String> termsWithoutCache = userTermsList.stream()
                .map(ut -> ut.getTerms().getTermName())
                .filter(term -> !hasCache(userId, term))
                .limit(maxTermsPerUser)  // 최대 개수 제한
                .collect(Collectors.toList());

        if (termsWithoutCache.isEmpty()) {
            log.debug("모든 용어에 캐시 존재: userId={}", userId);
            return 0;
        }

        log.info("퀴즈 생성 시작: userId={}, terms={}", userId, termsWithoutCache);

        // 3. 각 용어별 퀴즈 생성 및 캐싱
        int generatedCount = 0;
        for (String term : termsWithoutCache) {
            try {
                QuizResDto quizResponse = aiServerClient.generateQuiz(term, 3);

                String cacheKey = buildCacheKey(userId, term);
                redisTemplate.opsForValue().set(cacheKey, quizResponse, CACHE_TTL);

                generatedCount++;
                log.debug("퀴즈 캐싱 완료: userId={}, term={}", userId, term);

                // AI 서버 부하 방지를 위한 짧은 대기
                Thread.sleep(100);  // 0.1초

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("퀴즈 생성 중단: userId={}", userId, e);
                break;

            } catch (Exception e) {
                log.error("퀴즈 생성 실패: userId={}, term={}", userId, term, e);
                // 실패해도 다음 용어 계속 처리
            }
        }

        return generatedCount;
    }

    /**
     * 캐시 존재 여부 확인
     */
    private boolean hasCache(Long userId, String term) {
        String cacheKey = buildCacheKey(userId, term);
        return redisTemplate.hasKey(cacheKey);
    }

    /**
     * 캐시 키 생성
     */
    private String buildCacheKey(Long userId, String term) {
        return CACHE_KEY_PREFIX + userId + ":" + term;
    }

    /**
     * 캐시 통계 조회 (모니터링용)
     */
    public CacheStatistics getCacheStatistics() {
        // Redis의 모든 퀴즈 키 조회
        var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        int totalCachedQuizzes = keys != null ? keys.size() : 0;

        // 활성 사용자 수
        LocalDateTime activeThreshold = LocalDateTime.now().minusDays(activeDaysThreshold);
        long activeUserCount = userRepository.countActiveUsersSince(activeThreshold);

        // 전체 용어 수
        long totalTerms = userTermsRepository.count();

        double cacheHitRate = totalTerms > 0
                ? (double) totalCachedQuizzes / totalTerms * 100
                : 0;

        return new CacheStatistics(
                totalCachedQuizzes,
                activeUserCount,
                totalTerms,
                cacheHitRate
        );
    }

    /**
     * 캐시 통계 DTO
     */
    @Data
    @AllArgsConstructor
    public static class CacheStatistics {
        private int totalCachedQuizzes;     // 캐시된 퀴즈 총 개수
        private long activeUserCount;       // 활성 사용자 수
        private long totalTerms;            // 전체 용어 수
        private double cacheHitRate;        // 예상 캐시 히트율 (%)
    }
}
