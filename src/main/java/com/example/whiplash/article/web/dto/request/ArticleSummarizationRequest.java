package com.example.whiplash.article.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummarizationRequest {

    @NotEmpty(message = "기사 ID 목록은 필수입니다.")
    @Size(min = 1, max = 100, message = "기사 ID는 1개 이상 100개 이하여야 합니다.")
    private List<String> articleIds;

    @NotNull(message = "요청 시간은 필수입니다.")
    private LocalDateTime timestamp;
}