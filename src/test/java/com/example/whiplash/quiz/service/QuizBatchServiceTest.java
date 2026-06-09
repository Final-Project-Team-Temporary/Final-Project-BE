package com.example.whiplash.quiz.service;

import com.example.whiplash.POJOTestSupport;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class QuizBatchServiceTest extends POJOTestSupport {

    @Mock private UserRepository userRepository;
    @Mock private UserTermsRepository userTermsRepository;
    @Mock private TermQuizPoolRepository termQuizPoolRepository;
    @Mock private TermQuizPoolService termQuizPoolService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    // 테스트에서는 호출 즉시 동기 실행하는 executor로 대체
    private final Executor syncExecutor = Runnable::run;

    @InjectMocks
    private QuizBatchService quizBatchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(quizBatchService, "activeDaysThreshold", 30);
        ReflectionTestUtils.setField(quizBatchService, "maxTermsPerBatch", 50);
        ReflectionTestUtils.setField(quizBatchService, "quizTaskExecutor", syncExecutor);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    private List<QuizDto> makeQuizzes(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new QuizDto("Q" + i, List.of("A", "B", "C", "D"), 0, "E" + i))
                .toList();
    }

    @Test
    @DisplayName("배치는 고유 termName 단위로 처리하며, 풀이 없는 용어만 AI를 호출한다")
    void generateQuizzesForAllTerms_callsAiOnlyForTermsWithoutPool() {
        // given
        given(userTermsRepository.findDistinctTermNames())
                .willReturn(List.of("ETF", "PER", "금리"));

        // ETF는 이미 풀 있음, PER·금리는 없음
        given(termQuizPoolRepository.existsByTermName("ETF")).willReturn(true);
        given(termQuizPoolRepository.existsByTermName("PER")).willReturn(false);
        given(termQuizPoolRepository.existsByTermName("금리")).willReturn(false);

        given(termQuizPoolService.generateAndPersist(eq("PER"), anyString()))
                .willReturn(makeQuizzes(12));
        given(termQuizPoolService.generateAndPersist(eq("금리"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then: ETF는 스킵, PER·금리만 생성
        verify(termQuizPoolService, never()).generateAndPersist(eq("ETF"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("PER"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("금리"), anyString());
    }

    @Test
    @DisplayName("모든 용어에 풀이 있으면 AI 호출이 발생하지 않는다")
    void generateQuizzesForAllTerms_skipsAll_whenAllPoolsExist() {
        // given
        given(userTermsRepository.findDistinctTermNames())
                .willReturn(List.of("ETF", "PER"));
        given(termQuizPoolRepository.existsByTermName("ETF")).willReturn(true);
        given(termQuizPoolRepository.existsByTermName("PER")).willReturn(true);

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("저장된 용어가 없으면 아무 처리도 하지 않는다")
    void generateQuizzesForAllTerms_doesNothing_whenNoTerms() {
        given(userTermsRepository.findDistinctTermNames()).willReturn(List.of());

        quizBatchService.generateQuizzesForAllTerms();

        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("일부 용어의 생성이 실패해도 나머지 용어는 계속 처리한다")
    void generateQuizzesForAllTerms_continuesOnFailure() {
        // given
        given(userTermsRepository.findDistinctTermNames())
                .willReturn(List.of("ETF", "PER", "금리"));
        given(termQuizPoolRepository.existsByTermName(anyString())).willReturn(false);

        // ETF 생성 실패
        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willThrow(new RuntimeException("AI 서버 오류"));
        given(termQuizPoolService.generateAndPersist(eq("PER"), anyString()))
                .willReturn(makeQuizzes(12));
        given(termQuizPoolService.generateAndPersist(eq("금리"), anyString()))
                .willReturn(makeQuizzes(12));

        // when: 예외 전파 없이 완료돼야 함
        quizBatchService.generateQuizzesForAllTerms();

        // then: PER, 금리는 생성됨
        verify(termQuizPoolService).generateAndPersist(eq("PER"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("금리"), anyString());
    }

    // ───────────────────────────────────────────────
    // getCacheStatistics
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("getCacheStatistics는 MongoDB·Redis·DB 통계를 올바르게 집계한다")
    void getCacheStatistics_returnsCorrectAggregation() {
        // given
        given(termQuizPoolRepository.count()).willReturn(8L);
        given(userTermsRepository.findDistinctTermNames()).willReturn(List.of("ETF", "PER", "금리", "환율", "채권", "주식", "배당", "EPS", "PBR", "ROE"));
        given(redisTemplate.keys(QuizCacheConstants.POOL_KEY_PREFIX + "*"))
                .willReturn(Set.of("quiz:pool:ETF", "quiz:pool:PER", "quiz:pool:금리"));
        given(userRepository.countActiveUsersSince(any())).willReturn(5L);

        // when
        QuizBatchService.CacheStatistics stats = quizBatchService.getCacheStatistics();

        // then
        assertThat(stats.getTotalTermsInMongo()).isEqualTo(8);
        assertThat(stats.getRedisHotTerms()).isEqualTo(3);
        assertThat(stats.getActiveUserCount()).isEqualTo(5L);
        assertThat(stats.getTotalDistinctTerms()).isEqualTo(10L);
        assertThat(stats.getCoverageRate()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("고유 용어가 없으면 coverageRate는 0이다")
    void getCacheStatistics_zeroTerms_coverageRateIsZero() {
        // given
        given(termQuizPoolRepository.count()).willReturn(0L);
        given(userTermsRepository.findDistinctTermNames()).willReturn(List.of());
        given(redisTemplate.keys(QuizCacheConstants.POOL_KEY_PREFIX + "*")).willReturn(null);
        given(userRepository.countActiveUsersSince(any())).willReturn(0L);

        // when
        QuizBatchService.CacheStatistics stats = quizBatchService.getCacheStatistics();

        // then
        assertThat(stats.getCoverageRate()).isEqualTo(0.0);
        assertThat(stats.getRedisHotTerms()).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis keys가 null이면 redisHotTerms는 0이다")
    void getCacheStatistics_nullRedisKeys_redisHotTermsIsZero() {
        // given
        given(termQuizPoolRepository.count()).willReturn(3L);
        given(userTermsRepository.findDistinctTermNames()).willReturn(List.of("ETF", "PER", "금리"));
        given(redisTemplate.keys(anyString())).willReturn(null);
        given(userRepository.countActiveUsersSince(any())).willReturn(2L);

        // when
        QuizBatchService.CacheStatistics stats = quizBatchService.getCacheStatistics();

        // then
        assertThat(stats.getRedisHotTerms()).isEqualTo(0);
        assertThat(stats.getCoverageRate()).isEqualTo(100.0);
    }
}
