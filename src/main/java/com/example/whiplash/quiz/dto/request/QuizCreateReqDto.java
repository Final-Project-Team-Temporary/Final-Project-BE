package com.example.whiplash.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateReqDto {
    private String keyword;
    private Integer count =  3; // 기본 3개
}
