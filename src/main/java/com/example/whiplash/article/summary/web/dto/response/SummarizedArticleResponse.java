package com.example.whiplash.article.summary.web.dto.response;

import java.util.List;

public record SummarizedArticleResponse(
    List<ArticleSummaryDTO> summaries,
    List<KeywordResDto> keywords,
    List<RelatedStockResDto> stocks,
    boolean isBookmarked
) {}
