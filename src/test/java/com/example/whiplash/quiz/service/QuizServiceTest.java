package com.example.whiplash.quiz.service;

import com.example.whiplash.POJOTestSupport;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class QuizServiceTest extends POJOTestSupport {

    @Mock private TermQuizPoolService termQuizPoolService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private AiServerClient aiServerClient;
    @Mock private UserTermsRepository userTermsRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private QuizService quizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    private List<QuizDto> makeQuizzes(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new QuizDto("Q" + i, List.of("A", "B", "C", "D"), 0, "E" + i))
                .toList();
    }

    private UserTerms makeUserTerms(String termName) {
        Terms terms = Terms.builder()
                .termName(termName)
                .AiExplanation("설명")
                .build();
        return UserTerms.builder().terms(terms).build();
    }

    // ───────────────────────────────────────────────
    // getQuiz — term 지정
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("term을 지정하면 해당 용어의 풀에서 샘플링하여 반환한다")
    void getQuiz_withTerm_returnsQuizFromPool() {
        // given
        List<QuizDto> pool = makeQuizzes(12);
        List<QuizDto> sampled = makeQuizzes(3);
        given(termQuizPoolService.getQuizPool("ETF")).willReturn(pool);
        given(termQuizPoolService.sampleQuizzes(pool, QuizCacheConstants.DEFAULT_SAMPLE_SIZE))
                .willReturn(sampled);

        // when
        QuizResDto result = quizService.getQuiz(1L, "ETF");

        // then
        assertThat(result.getTerm()).isEqualTo("ETF");
        assertThat(result.getQuizzes()).hasSize(3);
    }

    // ───────────────────────────────────────────────
    // getQuiz — term 미지정 (랜덤)
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("term이 null이면 사용자 저장 용어 중 랜덤으로 선택한다")
    void getQuiz_withNullTerm_picksRandomTerm() {
        // given
        List<UserTerms> userTermsList = List.of(makeUserTerms("PER"));
        given(userTermsRepository.findByUserId(1L)).willReturn(userTermsList);

        List<QuizDto> pool = makeQuizzes(12);
        List<QuizDto> sampled = makeQuizzes(3);
        given(termQuizPoolService.getQuizPool("PER")).willReturn(pool);
        given(termQuizPoolService.sampleQuizzes(pool, QuizCacheConstants.DEFAULT_SAMPLE_SIZE))
                .willReturn(sampled);

        // when
        QuizResDto result = quizService.getQuiz(1L, null);

        // then
        assertThat(result.getTerm()).isEqualTo("PER");
        assertThat(result.getQuizzes()).hasSize(3);
    }

    @Test
    @DisplayName("저장된 용어가 없으면 예외를 던진다")
    void getQuiz_withNullTerm_throwsWhenNoUserTerms() {
        given(userTermsRepository.findByUserId(1L)).willReturn(List.of());

        assertThatThrownBy(() -> quizService.getQuiz(1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("저장된 용어가 없습니다");
    }

    @Test
    @DisplayName("term이 빈 문자열이면 랜덤 용어로 동작한다")
    void getQuiz_withEmptyTerm_treatsAsRandom() {
        List<UserTerms> userTermsList = List.of(makeUserTerms("금리"));
        given(userTermsRepository.findByUserId(1L)).willReturn(userTermsList);

        List<QuizDto> pool = makeQuizzes(12);
        List<QuizDto> sampled = makeQuizzes(3);
        given(termQuizPoolService.getQuizPool("금리")).willReturn(pool);
        given(termQuizPoolService.sampleQuizzes(pool, QuizCacheConstants.DEFAULT_SAMPLE_SIZE))
                .willReturn(sampled);

        QuizResDto result = quizService.getQuiz(1L, "");

        assertThat(result.getTerm()).isEqualTo("금리");
    }

    // ───────────────────────────────────────────────
    // getArticleQuiz
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("기사 퀴즈 Redis 캐시 HIT 시 AI 서버를 호출하지 않는다")
    void getArticleQuiz_cacheHit_doesNotCallAi() {
        // given
        String articleId = "article-123";
        int count = 3;
        Article article = mock(Article.class);
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        String cacheKey = QuizCacheConstants.articleKey(articleId, count);
        QuizResDto cached = new QuizResDto(makeQuizzes(count), null);
        given(valueOperations.get(cacheKey)).willReturn(cached);

        // when
        QuizResDto result = quizService.getArticleQuiz(1L, articleId, count);

        // then
        assertThat(result).isEqualTo(cached);
        verify(aiServerClient, never()).getQuizzesByArticle(anyString(), anyInt());
    }

    @Test
    @DisplayName("기사 퀴즈 Redis 캐시 MISS 시 AI를 호출하고 결과를 캐시에 저장한다")
    void getArticleQuiz_cacheMiss_callsAiAndCachesResult() {
        // given
        String articleId = "article-456";
        int count = 3;
        Article article = mock(Article.class);
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        String cacheKey = QuizCacheConstants.articleKey(articleId, count);
        given(valueOperations.get(cacheKey)).willReturn(null);

        QuizResDto aiResponse = new QuizResDto(makeQuizzes(count), null);
        given(aiServerClient.getQuizzesByArticle(articleId, count)).willReturn(aiResponse);

        // when
        QuizResDto result = quizService.getArticleQuiz(1L, articleId, count);

        // then
        assertThat(result).isEqualTo(aiResponse);
        verify(aiServerClient).getQuizzesByArticle(articleId, count);
        verify(valueOperations).set(eq(cacheKey), eq(aiResponse), any(Duration.class));
    }

    @Test
    @DisplayName("존재하지 않는 기사 ID로 요청 시 예외를 던진다")
    void getArticleQuiz_articleNotFound_throwsException() {
        // given
        String articleId = "not-exist";
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizService.getArticleQuiz(1L, articleId, 3))
                .isInstanceOf(WhiplashException.class);
        verify(aiServerClient, never()).getQuizzesByArticle(anyString(), anyInt());
    }
}