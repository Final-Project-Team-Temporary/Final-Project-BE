package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.dto.request.QuizResultReqDto;
import com.example.whiplash.quiz.entity.QuizResult;
import com.example.whiplash.quiz.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;

    /**
     * 퀴즈 결과 저장
     */
    @Transactional
    public void saveQuizResult(Long userId, QuizResultReqDto request) {
        QuizResult result = QuizResult.builder()
                .userId(userId)
                .term(request.getTerm())
                .score(request.getScore())
                .totalQuestions(request.getTotalQuestions())
                .build();

        quizResultRepository.save(result);

        log.info("퀴즈 결과 저장: userId={}, term={}, score={}/{}",
                userId, request.getTerm(), request.getScore(), request.getTotalQuestions());
    }

}
