package com.example.whiplash.quiz.service;

import com.example.whiplash.POJOTestSupport;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.document.TermQuizPool;
import com.example.whiplash.quiz.dto.QuizDto;
import org.springframework.data.domain.Pageable;
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
import org.springframework.data.redis.core.SetOperations;
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
    @Mock private SetOperations<String, Object> setOperations;

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
        given(redisTemplate.opsForSet()).willReturn(setOperations);
    }

    private List<QuizDto> makeQuizzes(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new QuizDto("Q" + i, List.of("A", "B", "C", "D"), 0, "E" + i))
                .toList();
    }

    private TermQuizPool makePoolWithName(String termName) {
        // Mockito 중첩 stubbing 방지 — 실제 객체 사용
        return TermQuizPool.create(termName, List.of());
    }

    // ───────────────────────────────────────────────
    // generateQuizzesForAllTerms — 용어 선별 개선
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("배치는 활성 사용자 기반 우선순위 쿼리로 용어를 조회한다")
    void generateQuizzesForAllTerms_usesActiveUserPrioritizedQuery() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of("ETF", "PER"));
        given(termQuizPoolRepository.findAllWithTermNameOnly()).willReturn(List.of());
        given(termQuizPoolService.generateAndPersist(anyString(), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then: 기존 findDistinctTermNames 미호출, 새 쿼리 호출
        verify(userTermsRepository, never()).findDistinctTermNames();
        verify(userTermsRepository).findPrioritizedTermNames(any(), any());
    }

    @Test
    @DisplayName("배치는 MongoDB 존재 여부를 bulk 1회 조회한다")
    void generateQuizzesForAllTerms_usesBulkMongoCheck() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of("ETF", "PER", "금리"));
        // ETF는 이미 존재
        given(termQuizPoolRepository.findAllWithTermNameOnly())
                .willReturn(List.of(makePoolWithName("ETF")));
        given(termQuizPoolService.generateAndPersist(anyString(), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then: bulk 조회 1회, 개별 existsByTermName 미호출
        verify(termQuizPoolRepository).findAllWithTermNameOnly();
        verify(termQuizPoolRepository, never()).existsByTermName(anyString());
        // ETF는 스킵, PER·금리만 생성
        verify(termQuizPoolService, never()).generateAndPersist(eq("ETF"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("PER"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("금리"), anyString());
    }

    @Test
    @DisplayName("생성 실패한 용어는 Redis 실패 Set에 적재된다")
    void generateQuizzesForAllTerms_addsFailedTermToRedisSet() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of("ETF", "PER"));
        given(termQuizPoolRepository.findAllWithTermNameOnly()).willReturn(List.of());

        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willThrow(new RuntimeException("AI 서버 오류"));
        given(termQuizPoolService.generateAndPersist(eq("PER"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then: ETF만 실패 Set에 추가
        verify(setOperations).add(eq(QuizCacheConstants.BATCH_FAILED_SET_KEY), eq("ETF"));
        verify(setOperations, never()).add(eq(QuizCacheConstants.BATCH_FAILED_SET_KEY), eq("PER"));
    }

    @Test
    @DisplayName("조회된 용어가 없으면 아무 처리도 하지 않는다")
    void generateQuizzesForAllTerms_doesNothing_whenNoTerms() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of());

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then
        verify(termQuizPoolRepository, never()).findAllWithTermNameOnly();
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("모든 용어에 풀이 있으면 AI 호출이 발생하지 않는다")
    void generateQuizzesForAllTerms_skipsAll_whenAllPoolsExist() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of("ETF", "PER"));
        given(termQuizPoolRepository.findAllWithTermNameOnly())
                .willReturn(List.of(makePoolWithName("ETF"), makePoolWithName("PER")));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("일부 용어의 생성이 실패해도 나머지 용어는 계속 처리한다")
    void generateQuizzesForAllTerms_continuesOnFailure() {
        // given
        given(userTermsRepository.findPrioritizedTermNames(any(), any()))
                .willReturn(List.of("ETF", "PER", "금리"));
        given(termQuizPoolRepository.findAllWithTermNameOnly()).willReturn(List.of());

        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willThrow(new RuntimeException("AI 서버 오류"));
        given(termQuizPoolService.generateAndPersist(eq("PER"), anyString()))
                .willReturn(makeQuizzes(12));
        given(termQuizPoolService.generateAndPersist(eq("금리"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.generateQuizzesForAllTerms();

        // then
        verify(termQuizPoolService).generateAndPersist(eq("PER"), anyString());
        verify(termQuizPoolService).generateAndPersist(eq("금리"), anyString());
    }

    // ───────────────────────────────────────────────
    // retryFailedTerms
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("실패 Set이 비어있으면 재시도 처리를 하지 않는다")
    void retryFailedTerms_doesNothing_whenFailedSetIsEmpty() {
        // given
        given(setOperations.members(QuizCacheConstants.BATCH_FAILED_SET_KEY))
                .willReturn(Set.of());

        // when
        quizBatchService.retryFailedTerms();

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    @Test
    @DisplayName("재시도 성공 시 해당 용어를 실패 Set에서 제거한다")
    void retryFailedTerms_removesFromSet_whenRetrySucceeds() {
        // given
        given(setOperations.members(QuizCacheConstants.BATCH_FAILED_SET_KEY))
                .willReturn(Set.of("ETF"));
        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willReturn(makeQuizzes(12));

        // when
        quizBatchService.retryFailedTerms();

        // then
        verify(termQuizPoolService).generateAndPersist(eq("ETF"), anyString());
        verify(setOperations).remove(QuizCacheConstants.BATCH_FAILED_SET_KEY, "ETF");
    }

    @Test
    @DisplayName("재시도 실패 시 해당 용어를 실패 Set에서 제거하지 않는다")
    void retryFailedTerms_keepsInSet_whenRetryFails() {
        // given
        given(setOperations.members(QuizCacheConstants.BATCH_FAILED_SET_KEY))
                .willReturn(Set.of("ETF"));
        given(termQuizPoolService.generateAndPersist(eq("ETF"), anyString()))
                .willThrow(new RuntimeException("AI 서버 오류"));

        // when
        quizBatchService.retryFailedTerms();

        // then
        verify(setOperations, never()).remove(anyString(), any());
    }

    @Test
    @DisplayName("실패 Set null 반환 시 재시도 처리를 하지 않는다")
    void retryFailedTerms_doesNothing_whenFailedSetIsNull() {
        // given
        given(setOperations.members(QuizCacheConstants.BATCH_FAILED_SET_KEY))
                .willReturn(null);

        // when
        quizBatchService.retryFailedTerms();

        // then
        verify(termQuizPoolService, never()).generateAndPersist(anyString(), anyString());
    }

    // ───────────────────────────────────────────────
    // getCacheStatistics
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("getCacheStatistics는 MongoDB·Redis·DB 통계를 올바르게 집계한다")
    void getCacheStatistics_returnsCorrectAggregation() {
        // given
        given(termQuizPoolRepository.count()).willReturn(8L);
        given(userTermsRepository.findDistinctTermNames()).willReturn(
                List.of("ETF", "PER", "금리", "환율", "채권", "주식", "배당", "EPS", "PBR", "ROE"));
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
