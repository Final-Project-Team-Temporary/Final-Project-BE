package com.example.whiplash.quiz.service;

import com.example.whiplash.POJOTestSupport;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
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

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class QuizPreGenerationServiceTest extends POJOTestSupport {

    @Mock private TermQuizPoolService termQuizPoolService;
    @Mock private TermQuizPoolRepository termQuizPoolRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private UserTermsRepository userTermsRepository;

    @InjectMocks
    private QuizPreGenerationService quizPreGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
    // generateQuizAsync
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("MongoDB에 풀이 이미 있으면 generateAndPersist를 호출하지 않는다")
    void generateQuizAsync_skips_whenPoolAlreadyExists() {
        // given
        given(termQuizPoolRepository.existsByTermName("ETF")).willReturn(true);

        // when
        quizPreGenerationService.generateQuizAsync("ETF");

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("MongoDB에 풀이 없으면 generateAndPersist를 호출한다")
    void generateQuizAsync_generatesPool_whenNotExists() {
        // given
        given(termQuizPoolRepository.existsByTermName("금리")).willReturn(false);
        given(termQuizPoolService.generateAndPersist(eq("금리"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizPreGenerationService.generateQuizAsync("금리");

        // then
        verify(termQuizPoolService).generateAndPersist(eq("금리"),
                eq(QuizCacheConstants.poolKey("금리")));
    }

    @Test
    @DisplayName("generateAndPersist에서 예외가 발생해도 CompletableFuture는 정상 완료된다")
    void generateQuizAsync_completes_evenOnException() {
        // given
        given(termQuizPoolRepository.existsByTermName("환율")).willReturn(false);
        given(termQuizPoolService.generateAndPersist(eq("환율"), anyString()))
                .willThrow(new RuntimeException("AI 서버 오류"));

        // when & then: 예외 전파 없이 정상 완료
        quizPreGenerationService.generateQuizAsync("환율");
    }

    // ───────────────────────────────────────────────
    // generateMissingPoolsForUser
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("사용자의 풀 없는 용어를 최대 5개까지 생성한다")
    void generateMissingPoolsForUser_generatesUpToFiveTerms() {
        // given: 6개 용어 중 모두 풀 없음
        List<UserTerms> userTermsList = List.of(
                makeUserTerms("ETF"), makeUserTerms("PER"), makeUserTerms("금리"),
                makeUserTerms("환율"), makeUserTerms("채권"), makeUserTerms("주식")
        );
        given(userTermsRepository.findByUserId(1L)).willReturn(userTermsList);
        given(termQuizPoolRepository.existsByTermName(anyString())).willReturn(false);
        given(termQuizPoolService.generateAndPersist(anyString(), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizPreGenerationService.generateMissingPoolsForUser(1L);

        // then: 최대 5개만 호출
        verify(termQuizPoolService, times(5)).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("모든 용어에 풀이 있으면 generateAndPersist를 호출하지 않는다")
    void generateMissingPoolsForUser_skipsAll_whenAllPoolsExist() {
        // given
        List<UserTerms> userTermsList = List.of(makeUserTerms("ETF"), makeUserTerms("PER"));
        given(userTermsRepository.findByUserId(1L)).willReturn(userTermsList);
        given(termQuizPoolRepository.existsByTermName("ETF")).willReturn(true);
        given(termQuizPoolRepository.existsByTermName("PER")).willReturn(true);

        // when
        quizPreGenerationService.generateMissingPoolsForUser(1L);

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("저장된 용어가 없으면 아무 처리도 하지 않는다")
    void generateMissingPoolsForUser_doesNothing_whenNoUserTerms() {
        // given
        given(userTermsRepository.findByUserId(1L)).willReturn(List.of());

        // when
        quizPreGenerationService.generateMissingPoolsForUser(1L);

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("일부 용어 생성이 실패해도 나머지 용어는 계속 처리한다")
    void generateMissingPoolsForUser_continuesOnFailure() {
        // given: ETF 실패, PER·금리 성공
        List<UserTerms> userTermsList = List.of(
                makeUserTerms("ETF"), makeUserTerms("PER"), makeUserTerms("금리")
        );
        given(userTermsRepository.findByUserId(1L)).willReturn(userTermsList);
        given(termQuizPoolRepository.existsByTermName(anyString())).willReturn(false);
        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willThrow(new RuntimeException("AI 오류"));
        given(termQuizPoolService.generateAndPersist(eq("PER"), anyString()))
                .willReturn(makeQuizzes(12));
        given(termQuizPoolService.generateAndPersist(eq("금리"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizPreGenerationService.generateMissingPoolsForUser(1L);

        // then: PER, 금리는 생성됨
        verify(termQuizPoolService).generateAndPersist(eq("PER"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("금리"), anyString());
    }
}