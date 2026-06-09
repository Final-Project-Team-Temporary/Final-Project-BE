package com.example.whiplash.quiz.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final TermQuizPoolService termQuizPoolService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiServerClient aiServerClient;
    private final UserTermsRepository userTermsRepository;
    private final ArticleRepository articleRepository;

    /**
     * 퀴즈 조회 — term 지정 시 해당 용어, 없으면 저장된 용어 중 랜덤 선택
     */
    public QuizResDto getQuiz(Long userId, String term) {
        String targetTerm = (term != null && !term.isEmpty())
                ? term
                : pickRandomTerm(userId);

        log.info("퀴즈 조회: userId={}, term={}", userId, targetTerm);

        List<QuizDto> pool = termQuizPoolService.getQuizPool(targetTerm);
        List<QuizDto> sampled = termQuizPoolService.sampleQuizzes(pool, QuizCacheConstants.DEFAULT_SAMPLE_SIZE);

        return new QuizResDto(sampled, targetTerm);
    }

    /**
     * 기사 기반 퀴즈 조회 — 기사 퀴즈는 용어와 무관하므로 별도 캐시 유지
     */
    public QuizResDto getArticleQuiz(Long userId, String articleId, Integer count) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

        String cacheKey = QuizCacheConstants.articleKey(articleId, count);

        QuizResDto cached = (QuizResDto) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("기사 퀴즈 캐시 HIT: articleId={}", articleId);
            return cached;
        }

        log.info("기사 퀴즈 생성: articleId={}, count={}", articleId, count);
        QuizResDto response = aiServerClient.getQuizzesByArticle(articleId, count);
        redisTemplate.opsForValue().set(cacheKey, response, QuizCacheConstants.POOL_TTL);

        return response;
    }

    private String pickRandomTerm(Long userId) {
        List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);
        if (userTermsList.isEmpty()) {
            throw new RuntimeException("저장된 용어가 없습니다. 먼저 용어를 저장해주세요.");
        }
        Terms randomTerm = userTermsList.get(new Random().nextInt(userTermsList.size())).getTerms();
        log.debug("랜덤 용어 선택: userId={}, term={}", userId, randomTerm.getTermName());
        return randomTerm.getTermName();
    }
}