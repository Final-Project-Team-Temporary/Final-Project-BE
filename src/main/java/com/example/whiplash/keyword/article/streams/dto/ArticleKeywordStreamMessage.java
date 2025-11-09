package com.example.whiplash.keyword.article.streams.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArticleKeywordStreamMessage(
        @JsonProperty("articleId")
        String articleId,

        @JsonProperty("terms")
        List<String> terms
) {
    public ArticleKeywordStreamMessage {
        if (articleId == null || articleId.isBlank()) {
            throw new IllegalArgumentException("articleId는 null이거나 비어있을 수 없습니다");
        }
        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("terms는 null이거나 비어있을 수 없습니다");
        }
    }
}
