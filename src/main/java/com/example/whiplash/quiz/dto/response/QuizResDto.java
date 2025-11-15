package com.example.whiplash.quiz.dto.response;

import com.example.whiplash.quiz.dto.QuizDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResDto implements Serializable {

    private List<QuizDto> quizzes;

    private LocalDateTime createdAt;

    private String term;

    public QuizResDto(List<QuizDto> quizzes, String term) {
        this.quizzes = quizzes;
        this.term = term;
        this.createdAt = LocalDateTime.now();
    }
}
