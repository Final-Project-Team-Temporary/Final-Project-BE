package com.example.whiplash.quiz.service;

import com.example.whiplash.MongoRedisTestSupport;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.document.TermQuizPool;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.quiz.repository.TermQuizPoolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TermQuizPoolServiceTest extends MongoRedisTestSupport {

    @Autowired
    private TermQuizPoolService termQuizPoolService;

    @Autowired
    private TermQuizPoolRepository termQuizPoolRepository;

    @MockBean
    private AiServerClient aiServerClient;

    private List<QuizDto> makeQuizzes(int count) {
        // ArrayList 사용: IntStream.toList()는 final 불변 리스트를 반환하여
        // GenericJackson2JsonRedisSerializer(NON_FINAL 타입)의 역직렬화 실패를 유발함
        ArrayList<QuizDto> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new QuizDto("질문" + i, new ArrayList<>(List.of("A", "B", "C", "D")), i % 4, "해설" + i));
        }
        return list;
    }

    // ───────────────────────────────────────────────
    // getQuizPool: Redis HIT
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("Redis에 퀴즈 풀이 있으면 MongoDB와 AI를 호출하지 않는다")
    void getQuizPool_redisHit_skipsMongoAndAi() {
        // given: Redis에 직접 풀 저장
        String term = "ETF";
        List<QuizDto> redisPool = makeQuizzes(12);
        redisTemplate.opsForValue().set(
                QuizCacheConstants.poolKey(term),
                redisPool,
                QuizCacheConstants.POOL_TTL
        );

        // when
        List<QuizDto> result = termQuizPoolService.getQuizPool(term);

        // then
        assertThat(result).hasSize(12);
        verify(aiServerClient, never()).generateQuiz(anyString(), anyInt());
        assertThat(termQuizPoolRepository.existsByTermName(term)).isFalse(); // MongoDB 건드리지 않음
    }

    // ───────────────────────────────────────────────
    // getQuizPool: Redis MISS → MongoDB HIT
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("Redis에 없고 MongoDB에 있으면 MongoDB를 조회하고 Redis에 저장한다")
    void getQuizPool_redisMiss_mongoHit_savesToRedis() {
        // given: MongoDB에만 풀 저장
        String term = "PER";
        termQuizPoolRepository.save(TermQuizPool.create(term, makeQuizzes(12)));

        // when
        List<QuizDto> result = termQuizPoolService.getQuizPool(term);

        // then
        assertThat(result).hasSize(12);
        verify(aiServerClient, never()).generateQuiz(anyString(), anyInt());

        // Redis에 채워졌는지 확인
        Object cached = redisTemplate.opsForValue().get(QuizCacheConstants.poolKey(term));
        assertThat(cached).isNotNull();
    }

    // ───────────────────────────────────────────────
    // getQuizPool: Redis MISS → MongoDB MISS → AI 호출
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("Redis와 MongoDB 모두 없으면 AI를 호출하고 양쪽에 저장한다")
    void getQuizPool_bothMiss_callsAiAndPersistsBothStores() {
        // given
        String term = "금리";
        List<QuizDto> aiQuizzes = makeQuizzes(12);
        given(aiServerClient.generateQuiz(term, QuizCacheConstants.POOL_SIZE))
                .willReturn(new QuizResDto(aiQuizzes, term));

        // when
        List<QuizDto> result = termQuizPoolService.getQuizPool(term);

        // then: 반환값 확인
        assertThat(result).hasSize(12);
        verify(aiServerClient).generateQuiz(term, QuizCacheConstants.POOL_SIZE);

        // MongoDB 저장 확인
        assertThat(termQuizPoolRepository.existsByTermName(term)).isTrue();
        assertThat(termQuizPoolRepository.findByTermName(term).orElseThrow().getQuizzes()).hasSize(12);

        // Redis 저장 확인
        Object cached = redisTemplate.opsForValue().get(QuizCacheConstants.poolKey(term));
        assertThat(cached).isNotNull();
    }

    // ───────────────────────────────────────────────
    // generateAndPersist: 이미 MongoDB에 있을 때 upsert
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("generateAndPersist 호출 시 MongoDB에 이미 있으면 퀴즈가 갱신된다")
    void generateAndPersist_updatesExistingMongo() {
        // given: MongoDB에 3개짜리 풀
        String term = "환율";
        termQuizPoolRepository.save(TermQuizPool.create(term, makeQuizzes(3)));

        List<QuizDto> newQuizzes = makeQuizzes(12);
        given(aiServerClient.generateQuiz(term, QuizCacheConstants.POOL_SIZE))
                .willReturn(new QuizResDto(newQuizzes, term));

        // when
        termQuizPoolService.generateAndPersist(term, QuizCacheConstants.poolKey(term));

        // then
        TermQuizPool updated = termQuizPoolRepository.findByTermName(term).orElseThrow();
        assertThat(updated.getQuizzes()).hasSize(12);
    }

    // ───────────────────────────────────────────────
    // sampleQuizzes
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("풀 크기가 요청 수보다 크면 랜덤으로 N개만 반환한다")
    void sampleQuizzes_returnsNItems_whenPoolIsLarger() {
        List<QuizDto> pool = makeQuizzes(12);

        List<QuizDto> sample = termQuizPoolService.sampleQuizzes(pool, 3);

        assertThat(sample).hasSize(3);
        assertThat(pool).containsAll(sample); // 풀 원소에서 선택됐는지 확인
    }

    @Test
    @DisplayName("풀 크기가 요청 수보다 작거나 같으면 전체를 반환한다")
    void sampleQuizzes_returnsAll_whenPoolIsSmallerOrEqual() {
        List<QuizDto> pool = makeQuizzes(2);

        List<QuizDto> sample = termQuizPoolService.sampleQuizzes(pool, 5);

        assertThat(sample).hasSize(2);
    }

    @Test
    @DisplayName("sampleQuizzes는 원본 풀을 변경하지 않는다")
    void sampleQuizzes_doesNotMutateOriginalPool() {
        List<QuizDto> pool = makeQuizzes(12);
        int originalSize = pool.size();

        termQuizPoolService.sampleQuizzes(pool, 3);

        assertThat(pool).hasSize(originalSize);
    }
}