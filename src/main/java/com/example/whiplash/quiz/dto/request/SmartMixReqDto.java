package com.example.whiplash.quiz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SmartMixReqDto {

    @Min(value = 3, message = "최소 3개 이상의 문제가 필요합니다.")
    @Max(value = 30, message = "최대 30개까지 가능합니다.")
    private Integer totalQuestions = 10;  // 기본값 10개
}
