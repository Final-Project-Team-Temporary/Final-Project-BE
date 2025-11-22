package com.example.whiplash.article.summary.web.dto.response;

import lombok.Builder;

@Builder
public record KeywordResDto(
        String term,
        String termSummary
) {
}
