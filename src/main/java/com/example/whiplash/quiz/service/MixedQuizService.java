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
        QuizResDto cached = (QuizResDto) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.debug("캐시 히트: userId={}, term={}", userId, term);
            return cached.getQuizzes();
        }

        // 2. 캐시 미스 - AI 서버 실시간 생성
        log.warn("캐시 미스 - 실시간 생성: userId={}, term={}", userId, term);

        try {
            QuizResDto freshQuiz = aiServerClient.generateQuiz(term, 3);

            // 캐시 저장 (7일)
            redisTemplate.opsForValue().set(
                    cacheKey,
                    freshQuiz,
                    java.time.Duration.ofDays(7)
            );

            return freshQuiz.getQuizzes();

        } catch (Exception e) {
            log.error("퀴즈 생성 실패: userId={}, term={}", userId, term, e);
            throw new RuntimeException("퀴즈를 생성할 수 없습니다: " + term);
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
}
