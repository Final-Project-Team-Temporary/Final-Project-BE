package com.example.whiplash.log.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TermQuizSolveRequest(
        @NotEmpty(message = "퀴즈 결과는 비어있을 수 없습니다")
        @Valid
        List<QuizResultItem> results
) {
    public record QuizResultItem(
            @NotNull(message = "문제는 필수입니다")
            String question,

            @NotEmpty(message = "선택지는 비어있을 수 없습니다")
            @Size(min = 2, max = 5, message = "선택지는 2~5개여야 합니다")
            List<String> options,

            @NotNull(message = "정답 인덱스는 필수입니다")
            Integer answerIndex,

            @NotNull(message = "사용자 답변 인덱스는 필수입니다")
            Integer userAnswerIndex,

            String explanation,

            @NotNull(message = "용어는 필수입니다")
            String term
    ) {}
}
