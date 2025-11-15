package com.example.whiplash.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MixedQuizResDto {

    private List<QuizWithTerm> quizzes;  // 퀴즈 목록

    private String terms;  // "ETF, ROE, PER, PBR"

    private Integer totalQuestions;  // 총 문제 수

    private Integer estimatedTime;  // 예상 소요 시간 (분)

    private LocalDateTime generatedAt;

    /**
     * Quiz + 용어명 포함
     */
    @Data
    @AllArgsConstructor
    public static class QuizWithTerm {
        private String question;
        private List<String> options;
        private Integer answerIndex;
        private String explanation;
        private String term;  // ⭐ 어떤 용어의 문제인지
    }
}
