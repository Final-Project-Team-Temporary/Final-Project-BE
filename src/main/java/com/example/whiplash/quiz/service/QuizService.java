package com.example.whiplash.quiz.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AiServerClient aiServerClient;
    private final UserTermsRepository userTermsRepository;
    private final QuizPreGenerationService quizPreGenerationService;
    private final ArticleRepository articleRepository;

    private static final String CACHE_KEY_PREFIX = "quiz:single:";
    private static final String ARTICLE_CACHE_KEY_PREFIX = "quiz:article:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    /**
     * 퀴즈 조회 - 캐시 우선 전략
     *
     * @param userId 사용자 ID
     * @param term 용어 (null이면 랜덤)
     * @return QuizResponse
     */
    public QuizResDto getQuiz(Long userId, String term) {

        if (term != null && !term.isEmpty()) {
            // Case 1: 특정 용어의 퀴즈
            return getQuizForSpecificTerm(userId, term);
        } else {
            // Case 2: 랜덤 퀴즈
            return getRandomQuiz(userId);
        }
    }

    /**
     * 기사 기반 퀴즈 조회
     * @param userId 사용자 ID
     * @param articleId 기사 ID
     * @param count 생성할 퀴즈 개수
     * @return QuizResDto
     */
    public QuizResDto getArticleQuiz(Long userId, String articleId, Integer count) {

        // 1. 기사 존재 여부 확인
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

        // 2. 캐시 키 생성 (count 포함)
        String cacheKey = buildArticleQuizKey(userId, articleId, count);

        // 3. Redis 캐시 확인
        QuizResDto cached = (QuizResDto) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("✅ 캐시 히트: userId={}, articleId={}, count={}",
                    userId, articleId, count);
            return cached;
        }

        // 4. 캐시 미스 - AI 서버 호출
        log.warn("⚠️ 캐시 미스 - 실시간 생성: userId={}, articleId={}, count={}",
                userId, articleId, count);

        QuizResDto response = generateArticleQuiz(userId, articleId, count);

        // 5. 캐시 저장
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);

        log.info("퀴즈 생성 완료 및 캐싱: userId={}, articleId={}, count={}",
                userId, articleId, count);

        return response;
    }

    /**
     * 특정 용어의 퀴즈 조회 (캐시 우선)
     */
    private QuizResDto getQuizForSpecificTerm(Long userId, String term) {
        String cacheKey = buildCacheKey(userId, term);

        // 1. Redis 캐시 확인
        QuizResDto cached = (QuizResDto) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("✅ 캐시 히트! userId={}, term={}", userId, term);
            return cached;
        }

        // 2. 캐시 미스 - 실시간 생성 필요
        log.warn("⚠️ 캐시 미스 - 실시간 생성: userId={}, term={}", userId, term);
        return generateQuizRealtime(userId, term);
    }

    /**
     * 실시간 퀴즈 생성 (캐시 미스 시)
     */
    private QuizResDto generateQuizRealtime(Long userId, String term) {
        try {
            long startTime = System.currentTimeMillis();

            // AI 서버 호출 (3초 대기)
            QuizResDto freshQuiz = aiServerClient.generateQuiz(term, 3);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("🤖 AI 서버 퀴즈 생성 완료: term={}, elapsed={}ms", term, elapsedTime);

            // 캐시 저장 (다음 요청 대비)
            String cacheKey = buildCacheKey(userId, term);
            redisTemplate.opsForValue().set(cacheKey, freshQuiz, CACHE_TTL);

            // 백그라운드로 추가 퀴즈 미리 생성 (다음을 위해)
            quizPreGenerationService.generateMoreQuizzesForUser(userId);

            return freshQuiz;

        } catch (Exception e) {
            log.error("❌ 퀴즈 생성 실패: userId={}, term={}", userId, term, e);
            throw new RuntimeException("퀴즈를 생성할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 실시간 기사 퀴즈 생성 (캐시 미스 시)
     */
    private QuizResDto generateArticleQuiz(Long userId, String articleId, Integer count) {
        try {
            long startTime = System.currentTimeMillis();

            // AI 서버 호출
            QuizResDto freshQuiz = aiServerClient.getQuizzesByArticle(articleId, count);

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("🤖 AI 서버 퀴즈 생성 완료: articleId={}, count={}, elapsed={}ms",
                    articleId, count, elapsedTime);

            return freshQuiz;

        } catch (Exception e) {
            log.error("❌ 퀴즈 생성 실패: userId={}, articleId={}, count={}", userId, articleId, count, e);
            throw new WhiplashException(ErrorStatus.AI_SERVER_ERROR);
        }
    }

    /**
     * 랜덤 퀴즈 조회 (사용자가 저장한 용어 중에서)
     */
    private QuizResDto getRandomQuiz(Long userId) {
        // 1. 사용자가 저장한 용어 목록 조회
        List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);

        if (userTermsList.isEmpty()) {
            throw new RuntimeException("저장된 용어가 없습니다. 먼저 용어를 저장해주세요.");
        }

        // 2. 랜덤으로 하나 선택
        Random random = new Random();
        Terms randomTerm = userTermsList.get(random.nextInt(userTermsList.size())).getTerms();

        log.info("🎲 랜덤 용어 선택: userId={}, term={}", userId, randomTerm.getTermName());

        // 3. 해당 용어의 퀴즈 조회 (캐시 우선)
        return getQuizForSpecificTerm(userId, randomTerm.getTermName());
    }

    /**
     * 캐시 키 생성
     */
    private String buildCacheKey(Long userId, String term) {
        return CACHE_KEY_PREFIX + userId + ":" + term;
    }

    /**
     * 기사 기반 퀴즈 저장용 캐시 키 생성
     * count를 포함하여 동일한 기사라도 다른 개수의 퀴즈는 별도로 캐싱
     */
    private String buildArticleQuizKey(Long userId, String articleId, Integer count) {
        return ARTICLE_CACHE_KEY_PREFIX + userId + ":" + articleId + ":" + count;
    }
}
