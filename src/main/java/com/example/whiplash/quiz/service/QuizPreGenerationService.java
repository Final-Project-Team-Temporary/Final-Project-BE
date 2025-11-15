package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizPreGenerationService {

    private final AiServerClient aiServerClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserTermsRepository userTermsRepository;

    private static final String CACHE_KEY_PREFIX = "quiz:single:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    /**
     * 비동기로 퀴즈 생성 및 캐싱
     * 사용자는 이 메서드 완료를 기다리지 않음!
     */
    @Async("quizTaskExecutor")  // 별도 스레드에서 실행
    public CompletableFuture<Void> generateQuizAsync(Long userId, String term) {
        String cacheKey = buildCacheKey(userId, term);

        log.info("비동기 퀴즈 생성 시작: userId={}, term={}", userId, term);

        try {
            // 1. 이미 캐시에 있는지 확인
            if (redisTemplate.hasKey(cacheKey)) {
                log.info("이미 캐시 존재, 생성 스킵: {}", cacheKey);
                return CompletableFuture.completedFuture(null);
            }

            // 2. AI 서버 호출 (3초 소요)
            long startTime = System.currentTimeMillis();
            QuizResDto quizResponse = aiServerClient.generateQuiz(term, 3);
            long elapsedTime = System.currentTimeMillis() - startTime;

            // 3. Redis에 캐싱
            redisTemplate.opsForValue().set(cacheKey, quizResponse, CACHE_TTL);

            log.info("비동기 퀴즈 생성 완료: userId={}, term={}, elapsed={}ms",
                    userId, term, elapsedTime);

        } catch (Exception e) {
            log.error("비동기 퀴즈 생성 실패: userId={}, term={}", userId, term, e);
            // 에러는 로깅만 하고 메인 플로우에 영향 주지 않음
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 사용자의 다른 용어들도 미리 퀴즈 생성 (선제적 캐싱)
     */
    @Async("quizTaskExecutor")
    public CompletableFuture<Void> generateMoreQuizzesForUser(Long userId) {
        log.info("🔮 사용자의 추가 퀴즈 미리 생성 시작: userId={}", userId);

        try {
            // 1. 사용자가 저장한 용어 중 캐시 없는 것 찾기
            List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);

            List<String> termsWithoutCache = userTermsList.stream()
                    .map(ut -> ut.getTerms().getTermName())
                    .filter(term -> !hasCache(userId, term))
                    .limit(5)  // 최대 5개만
                    .toList();

            if (termsWithoutCache.isEmpty()) {
                log.info("모든 용어에 캐시 존재: userId={}", userId);
                return CompletableFuture.completedFuture(null);
            }

            // 2. 캐시 없는 용어들 퀴즈 생성
            for (String term : termsWithoutCache) {
                generateQuizAsync(userId, term).join();  // 순차 생성
            }

            log.info("✅ 추가 퀴즈 생성 완료: userId={}, count={}", userId, termsWithoutCache.size());

        } catch (Exception e) {
            log.error("추가 퀴즈 생성 실패: userId={}", userId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 캐시 존재 여부 확인
     */
    private boolean hasCache(Long userId, String term) {
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + term;
        return redisTemplate.hasKey(cacheKey);
    }

    /**
     * 캐시 키 생성 규칙
     */
    private String buildCacheKey(Long userId, String term) {
        return CACHE_KEY_PREFIX + userId + ":" + term;
    }
}
