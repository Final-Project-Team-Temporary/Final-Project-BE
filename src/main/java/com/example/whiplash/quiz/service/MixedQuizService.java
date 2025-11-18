package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.request.MixedQuizReqDto;
import com.example.whiplash.quiz.dto.response.MixedQuizResDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MixedQuizService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AiServerClient aiServerClient;

    private static final String CACHE_KEY_PREFIX = "quiz:single:";

    /**
     * 커스텀 모의고사 생성
     */
    public MixedQuizResDto createMixedQuiz(Long userId, MixedQuizReqDto request) {
        log.info("커스텀 모의고사 생성: userId={}, terms={}, questionsPerTerm={}",
                userId, request.getTerms(), request.getQuestionsPerTerm());

        List<MixedQuizResDto.QuizWithTerm> mixedQuizzes = new ArrayList<>();

        // 1. 각 용어별로 퀴즈 조회
        for (String term : request.getTerms()) {
            List<QuizDto> termQuizzes = getQuizzesForTerm(userId, term);

            // 2. 요청한 개수만큼 랜덤 선택
            List<QuizDto> selectedQuizzes = selectRandomQuizzes(
                    termQuizzes,
                    request.getQuestionsPerTerm()
            );

            // 3. QuizWithTerm으로 변환 (용어명 추가)
            for (QuizDto quiz : selectedQuizzes) {
                mixedQuizzes.add(new MixedQuizResDto.QuizWithTerm(
                        quiz.getQuestion(),
                        quiz.getOptions(),
                        quiz.getAnswerIndex(),
                        quiz.getExplanation(),
                        term  // ⭐ 용어명 추가
                ));
            }
        }

        // 4. 섞기 (랜덤 순서)
        Collections.shuffle(mixedQuizzes);

        // 5. 응답 생성
        String termsStr = String.join(", ", request.getTerms());
        int totalQuestions = mixedQuizzes.size();
        int estimatedTime = calculateEstimatedTime(totalQuestions);

        log.info("커스텀 모의고사 생성 완료: userId={}, totalQuestions={}",
                userId, totalQuestions);

        return new MixedQuizResDto(
                mixedQuizzes,
                termsStr,
                totalQuestions,
                estimatedTime,
                LocalDateTime.now()
        );
    }

    /**
     * 특정 용어의 퀴즈 조회 (캐시 우선)
     */
    private List<QuizDto> getQuizzesForTerm(Long userId, String term) {
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + term;

        // 1. 캐시 확인
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            log.debug("캐시 히트: userId={}, term={}", userId, term);

            try {
                // Redis에서 QuizResDto로 역직렬화 (타입 정보 포함)
                if (cachedData instanceof QuizResDto) {
                    QuizResDto cached = (QuizResDto) cachedData;
                    log.info("캐싱 데이터: term={}, count={}", term, cached.getQuizzes().size());
                    return cached.getQuizzes();
                } else {
                    log.warn("예상치 못한 캐시 타입: {}", cachedData.getClass().getName());
                    // 잘못된 타입이면 캐시 삭제 후 재생성
                    redisTemplate.delete(cacheKey);
                    return generateAndCacheQuiz(userId, term, cacheKey);
                }
            } catch (Exception e) {
                // 역직렬화 실패 시 캐시 삭제 후 재생성
                log.warn("캐시 역직렬화 실패 - 캐시 삭제 후 재생성: userId={}, term={}, error={}",
                         userId, term, e.getMessage());
                redisTemplate.delete(cacheKey);

                // 재생성 후 반환
                return generateAndCacheQuiz(userId, term, cacheKey);
            }
        }

        // 2. 캐시 미스 - AI 서버 실시간 생성
        log.warn("캐시 미스 - 실시간 생성: userId={}, term={}", userId, term);
        return generateAndCacheQuiz(userId, term, cacheKey);
    }

    /**
     * 퀴즈 생성 및 캐싱 (공통 로직 분리)
     */
    private List<QuizDto> generateAndCacheQuiz(Long userId, String term, String cacheKey) {
        try {
            QuizResDto freshQuiz = aiServerClient.generateQuiz(term, 3);

            // 캐시 저장 (7일)
            redisTemplate.opsForValue().set(
                    cacheKey,
                    freshQuiz,
                    java.time.Duration.ofDays(7)
            );

            log.info("퀴즈 생성 및 캐싱 완료: userId={}, term={}, count={}",
                     userId, term, freshQuiz.getQuizzes().size());

            return freshQuiz.getQuizzes();

        } catch (Exception e) {
            log.error("퀴즈 생성 실패: userId={}, term={}", userId, term, e);
            throw new RuntimeException("퀴즈를 생성할 수 없습니다: " + term, e);
        }
    }

    /**
     * 퀴즈 목록에서 랜덤으로 N개 선택
     */
    private List<QuizDto> selectRandomQuizzes(List<QuizDto> quizzes, int count) {
        if (quizzes.size() <= count) {
            return new ArrayList<>(quizzes);  // 전체 반환
        }

        // 랜덤 셔플 후 앞에서 count개 선택
        List<QuizDto> shuffled = new ArrayList<>(quizzes);
        Collections.shuffle(shuffled);

        return shuffled.subList(0, count);
    }

    /**
     * 예상 소요 시간 계산 (문제당 평균 30초)
     */
    private int calculateEstimatedTime(int totalQuestions) {
        return (int) Math.ceil(totalQuestions * 0.5);  // 0.5분 = 30초
    }

    /**
     * 특정 사용자의 모든 퀴즈 캐시 삭제
     */
    public void clearUserQuizCache(Long userId) {
        String pattern = CACHE_KEY_PREFIX + userId + ":*";

        // 패턴에 맞는 키 찾기
        var keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("퀴즈 캐시 삭제 완료: userId={}, count={}", userId, keys.size());
        } else {
            log.info("삭제할 캐시가 없음: userId={}", userId);
        }
    }

    /**
     * 특정 용어의 퀴즈 캐시 삭제
     */
    public void clearTermQuizCache(Long userId, String term) {
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + term;

        Boolean deleted = redisTemplate.delete(cacheKey);

        log.info("용어 퀴즈 캐시 삭제: userId={}, term={}, deleted={}",
                 userId, term, deleted);
    }
}
