package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.constant.QuizCacheConstants;
import com.example.whiplash.quiz.dto.QuizDto;
import com.example.whiplash.quiz.dto.request.MixedQuizReqDto;
import com.example.whiplash.quiz.dto.response.MixedQuizResDto;
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

    private final TermQuizPoolService termQuizPoolService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 커스텀 모의고사 생성
     * 각 용어의 퀴즈 풀을 TermQuizPoolService를 통해 조회하고 랜덤 샘플링한다.
     */
    public MixedQuizResDto createMixedQuiz(Long userId, MixedQuizReqDto request) {
        log.info("커스텀 모의고사 생성: userId={}, terms={}, questionsPerTerm={}",
                userId, request.getTerms(), request.getQuestionsPerTerm());

        List<MixedQuizResDto.QuizWithTerm> mixedQuizzes = new ArrayList<>();

        for (String term : request.getTerms()) {
            List<QuizDto> pool = termQuizPoolService.getQuizPool(term);
            List<QuizDto> selected = termQuizPoolService.sampleQuizzes(pool, request.getQuestionsPerTerm());

            for (QuizDto quiz : selected) {
                mixedQuizzes.add(new MixedQuizResDto.QuizWithTerm(
                        quiz.getQuestion(),
                        quiz.getOptions(),
                        quiz.getAnswerIndex(),
                        quiz.getExplanation(),
                        term
                ));
            }
        }

        Collections.shuffle(mixedQuizzes);

        String termsStr = String.join(", ", request.getTerms());
        int totalQuestions = mixedQuizzes.size();
        int estimatedTime = (int) Math.ceil(totalQuestions * 0.5);

        log.info("커스텀 모의고사 생성 완료: userId={}, totalQuestions={}", userId, totalQuestions);

        return new MixedQuizResDto(mixedQuizzes, termsStr, totalQuestions, estimatedTime, LocalDateTime.now());
    }

    /**
     * 특정 용어의 Redis L1 캐시 삭제 (MongoDB 풀은 유지)
     * 퀴즈 내용을 강제 갱신하고 싶을 때 사용
     */
    public void clearTermQuizCache(String termName) {
        String key = QuizCacheConstants.poolKey(termName);
        Boolean deleted = redisTemplate.delete(key);
        log.info("용어 퀴즈 Redis 캐시 삭제: term={}, deleted={} (MongoDB 풀은 유지됨)", termName, deleted);
    }
}