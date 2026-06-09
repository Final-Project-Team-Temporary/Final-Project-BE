package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 신규 용어 저장 시 백그라운드에서 퀴즈 풀을 선제 생성하는 서비스.
 * 용어 단위 풀이 이미 존재하면 스킵한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizPreGenerationService {

    private final TermQuizPoolService termQuizPoolService;
    private final TermQuizPoolRepository termQuizPoolRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserTermsRepository userTermsRepository;

    /**
     * 단일 용어에 대해 비동기로 퀴즈 풀 생성.
     * 이미 MongoDB에 풀이 있으면 스킵한다.
     */
    @Async("quizTaskExecutor")
    public CompletableFuture<Void> generateQuizAsync(String termName) {
        if (termQuizPoolRepository.existsByTermName(termName)) {
            log.debug("퀴즈 풀 이미 존재, 스킵: term={}", termName);
            return CompletableFuture.completedFuture(null);
        }

        log.info("비동기 퀴즈 풀 생성: term={}", termName);
        try {
            String cacheKey = QuizCacheConstants.poolKey(termName);
            termQuizPoolService.generateAndPersist(termName, cacheKey);
        } catch (Exception e) {
            log.error("비동기 퀴즈 풀 생성 실패: term={}", termName, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 사용자가 저장한 용어 중 풀이 없는 것들을 백그라운드에서 생성.
     * 최대 5개까지만 처리하여 스레드 풀 포화를 방지한다.
     */
    @Async("quizTaskExecutor")
    public CompletableFuture<Void> generateMissingPoolsForUser(Long userId) {
        log.info("사용자 미보유 퀴즈 풀 선제 생성: userId={}", userId);

        try {
            List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);

            List<String> missingTerms = userTermsList.stream()
                    .map(ut -> ut.getTerms().getTermName())
                    .filter(term -> !termQuizPoolRepository.existsByTermName(term))
                    .limit(5)
                    .toList();

            if (missingTerms.isEmpty()) {
                log.debug("모든 용어에 퀴즈 풀 존재: userId={}", userId);
                return CompletableFuture.completedFuture(null);
            }

            for (String term : missingTerms) {
                try {
                    String cacheKey = QuizCacheConstants.poolKey(term);
                    termQuizPoolService.generateAndPersist(term, cacheKey);
                } catch (Exception e) {
                    log.error("퀴즈 풀 생성 실패: term={}", term, e);
                }
            }

            log.info("선제 퀴즈 풀 생성 완료: userId={}, count={}", userId, missingTerms.size());
        } catch (Exception e) {
            log.error("선제 생성 실패: userId={}", userId, e);
        }

        return CompletableFuture.completedFuture(null);
    }
}