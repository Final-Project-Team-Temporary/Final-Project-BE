package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.document.TermQuizPool;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 3단계 캐시 계층을 조율하는 핵심 서비스
 *
 * ① Redis (quiz:pool:{termName}, TTL 1h)   — L1 캐시
 * ② MongoDB (term_quiz_pools)              — 영구 퀴즈 풀
 * ③ AI 서버                               — 풀이 없을 때만 호출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TermQuizPoolService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TermQuizPoolRepository termQuizPoolRepository;
    private final AiServerClient aiServerClient;

    /**
     * 용어에 대한 퀴즈 풀을 가져온다.
     * Redis → MongoDB → AI 서버 순서로 조회하며, 하위 계층에서 조회된 데이터는 상위 계층에 채워준다.
     *
     * @param termName 용어명
     * @return 해당 용어의 전체 퀴즈 풀
     */
    public List<QuizDto> getQuizPool(String termName) {
        // ① Redis L1 캐시 확인
        String cacheKey = QuizCacheConstants.poolKey(termName);
        List<QuizDto> cached = getFromRedis(cacheKey);

        if (cached != null) {
            log.debug("Redis HIT: term={}", termName);
            return cached;
        }

        // ② MongoDB 조회
        Optional<TermQuizPool> poolDoc = termQuizPoolRepository.findByTermName(termName);

        if (poolDoc.isPresent()) {
            log.debug("MongoDB HIT: term={}", termName);
            List<QuizDto> quizzes = poolDoc.get().getQuizzes();
            saveToRedis(cacheKey, quizzes);
            return quizzes;
        }

        // ③ AI 서버 호출 및 저장
        log.info("AI 서버 호출 (풀 없음): term={}", termName);
        return generateAndPersist(termName, cacheKey);
    }

    /**
     * 풀에서 랜덤으로 N개의 퀴즈를 추출한다.
     */
    public List<QuizDto> sampleQuizzes(List<QuizDto> pool, int count) {
        if (pool.size() <= count) {
            return new ArrayList<>(pool);
        }
        List<QuizDto> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * 특정 용어의 풀을 AI 서버에서 새로 생성하고 MongoDB/Redis에 저장한다.
     * 배치나 외부에서 강제 갱신할 때 사용한다.
     */
    public List<QuizDto> generateAndPersist(String termName, String cacheKey) {
        QuizResDto aiResponse = aiServerClient.generateQuiz(termName, QuizCacheConstants.POOL_SIZE);
        List<QuizDto> quizzes = aiResponse.getQuizzes();

        // MongoDB upsert (이미 있으면 갱신, 없으면 삽입)
        termQuizPoolRepository.findByTermName(termName).ifPresentOrElse(
                existing -> {
                    existing.updateQuizzes(quizzes);
                    termQuizPoolRepository.save(existing);
                },
                () -> termQuizPoolRepository.save(TermQuizPool.create(termName, quizzes))
        );

        saveToRedis(cacheKey, quizzes);

        log.info("퀴즈 풀 생성 완료: term={}, count={}", termName, quizzes.size());
        return quizzes;
    }

    /**
     * 해당 용어의 퀴즈 풀이 MongoDB에 존재하는지 확인한다.
     */
    public boolean existsInMongo(String termName) {
        return termQuizPoolRepository.existsByTermName(termName);
    }

    /**
     * Redis에서 퀴즈 풀을 조회한다.
     * GenericJackson2JsonRedisSerializer 타입 정보 때문에 LinkedHashMap으로 역직렬화될 수 있어
     * QuizResDto 경유로 안전하게 변환한다.
     */
    @SuppressWarnings("unchecked")
    private List<QuizDto> getFromRedis(String cacheKey) {
        try {
            Object raw = redisTemplate.opsForValue().get(cacheKey);
            if (raw == null) {
                return null;
            }
            if (raw instanceof List) {
                return (List<QuizDto>) raw;
            }
            return null;
        } catch (Exception e) {
            log.warn("Redis 역직렬화 실패 — 캐시 삭제: key={}, error={}", cacheKey, e.getMessage());
            redisTemplate.delete(cacheKey);
            return null;
        }
    }

    private void saveToRedis(String cacheKey, List<QuizDto> quizzes) {
        try {
            redisTemplate.opsForValue().set(cacheKey, quizzes, QuizCacheConstants.POOL_TTL);
        } catch (Exception e) {
            log.warn("Redis 저장 실패 (무시): key={}, error={}", cacheKey, e.getMessage());
        }
    }
}