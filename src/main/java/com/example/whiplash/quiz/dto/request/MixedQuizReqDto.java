package com.example.whiplash.quiz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MixedQuizReqDto {

    @NotEmpty(message = "용어를 최소 2개 이상 선택해주세요.")
    @Size(min = 2, max = 10, message = "용어는 2개 이상 10개 이하로 선택해주세요.")
    private List<String> terms;  // ["ETF", "ROE", "PER"]

    @Min(value = 1, message = "각 용어당 최소 1개 문제가 필요합니다.")
    @Max(value = 5, message = "각 용어당 최대 5개 문제까지 가능합니다.")
    private Integer questionsPerTerm = 2;  // 기본값 2개

    private String difficulty = "medium";  // easy, medium, hard (향후 확장용)
}
