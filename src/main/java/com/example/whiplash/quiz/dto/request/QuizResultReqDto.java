package com.example.whiplash.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizResultReqDto {

    @NotBlank(message = "용어명은 필수입니다.")
    private String term;

    @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    private Integer score;

    @Min(value = 1, message = "총 문제 수는 1 이상이어야 합니다.")
    private Integer totalQuestions;
}
