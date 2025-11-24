package com.example.whiplash.log.quiz.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "term_quiz_solve_logs")
@Entity
public class TermQuizSolveLog extends QuizSolveLog {

    @Builder.Default
    @OneToMany(
        mappedBy = "termQuizSolveLog",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<QuizWithTermResult> results = new ArrayList<>();

    public void addResult(QuizWithTermResult result) {
        results.add(result);
        result.setTermQuizSolveLog(this);
    }
}
