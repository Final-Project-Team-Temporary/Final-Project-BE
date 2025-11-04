package com.example.whiplash.article.summary.web.dto.response;

import com.example.whiplash.article.original.service.ArticleRegisterInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummarizationResponse {

    private boolean success;
    private int processedCount;
    private List<String> failedIds;
    private List<String> jobIds;
    private String message;

    public static ArticleSummarizationResponse create(boolean success, ArticleRegisterInfo registerInfo, String message) {
        return ArticleSummarizationResponse.builder()
                .success(success)
                .processedCount(registerInfo.processedArticleIds().size())
                .failedIds(registerInfo.failedIds())
                .jobIds(registerInfo.jobIds())
                .message(message)
                .build();
    }

}