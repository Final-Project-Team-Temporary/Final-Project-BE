package com.example.whiplash.article.web.dto.response;

import java.util.List;

public record ArticleDetailResponse(
    List<ArticleSummaryDTO> summaries
) {}
