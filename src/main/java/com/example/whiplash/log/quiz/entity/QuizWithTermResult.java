package com.example.whiplash.log.quiz.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "quiz_with_term_results")
@Entity
public class QuizWithTermResult {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String question;

    @ElementCollection
    @CollectionTable(
            name = "quiz_result_options",
            joinColumns = @JoinColumn(name = "quiz_solve_log_id")
    )
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    private Integer answerIndex;

    private Integer userAnswerIndex;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private String term;

    private Boolean isCorrect;

    @JoinColumn(name = "quiz_solve_log_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TermQuizSolveLog termQuizSolveLog;

    public void setTermQuizSolveLog(TermQuizSolveLog termQuizSolveLog) {
        this.termQuizSolveLog = termQuizSolveLog;
    }
}
