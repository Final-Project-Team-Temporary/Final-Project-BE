package com.example.whiplash.log.quiz.dto.response;

import java.time.LocalDateTime;

public record TermQuizSolveResponse(
        Long id,
        Long userId,
        Integer score,
        Integer totalQuestions,
        LocalDateTime solvedAt
) {}
