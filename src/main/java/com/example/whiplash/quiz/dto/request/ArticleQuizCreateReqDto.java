package com.example.whiplash.quiz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleQuizCreateReqDto {

    @NotBlank(message = "기사 ID는 필수입니다")
    @JsonProperty("article_id")
    private String articleId;

    @Min(value = 1, message = "퀴즈 개수는 최소 1개 이상이어야 합니다")
    @Max(value = 10, message = "퀴즈 개수는 최대 10개까지 가능합니다")
    private Integer count = 3;
}
