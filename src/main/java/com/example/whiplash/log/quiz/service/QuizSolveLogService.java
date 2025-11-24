package com.example.whiplash.log.quiz.service;

import com.example.whiplash.daily.learning.entity.LearningPerformedEvent;
import com.example.whiplash.daily.learning.entity.LearningType;
import com.example.whiplash.global.event.DomainEventPublisher;
import com.example.whiplash.log.quiz.dto.request.TermQuizSolveRequest;
import com.example.whiplash.log.quiz.dto.response.TermQuizSolveResponse;
import com.example.whiplash.log.quiz.entity.QuizWithTermResult;
import com.example.whiplash.log.quiz.entity.TermQuizSolveLog;
import com.example.whiplash.log.quiz.repository.TermQuizSolveLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizSolveLogService {

    private final TermQuizSolveLogRepository termQuizSolveLogRepository;
    private final DomainEventPublisher eventPublisher;


    @Transactional
    public TermQuizSolveResponse solveTermQuiz(Long userId,
        TermQuizSolveRequest request,
        LocalDateTime solvedAt
    ) {

        return saveTermQuizSolveLog(userId, request, solvedAt);
    }

    private TermQuizSolveResponse saveTermQuizSolveLog(Long userId, TermQuizSolveRequest request,
        LocalDateTime solvedAt) {
        TermQuizSolveLog solveLog = TermQuizSolveLog.builder()
                .userId(userId)
                .score(0)
                .totalQuestions(request.results().size())
                .solvedAt(solvedAt)
                .build();

        int score = 0;
        for (TermQuizSolveRequest.QuizResultItem item : request.results()) {
            boolean isCorrect = item.answerIndex().equals(item.userAnswerIndex());
            if (isCorrect) {
                score++;
            }

            QuizWithTermResult result = QuizWithTermResult.builder()
                    .question(item.question())
                    .options(item.options())
                    .answerIndex(item.answerIndex())
                    .userAnswerIndex(item.userAnswerIndex())
                    .explanation(item.explanation())
                    .term(item.term())
                    .isCorrect(isCorrect)
                    .build();

            solveLog.addResult(result);
        }

        solveLog.updateScore(score);

        TermQuizSolveLog saved = termQuizSolveLogRepository.save(solveLog);

        log.info("퀴즈 풀이 결과 저장: userId={}, score={}/{}, solvedAt={}",
            userId, score, request.results().size(), solvedAt);

        return new TermQuizSolveResponse(
            saved.getId(),
            saved.getUserId(),
            saved.getScore(),
            saved.getTotalQuestions(),
            saved.getSolvedAt()
        );
    }
}
