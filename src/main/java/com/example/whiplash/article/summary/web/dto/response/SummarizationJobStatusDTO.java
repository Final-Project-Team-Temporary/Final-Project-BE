package com.example.whiplash.article.summary.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationJobStatusDTO {

    private String jobId;
    private String articleId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int attempts;
    private String errorMessage;
}