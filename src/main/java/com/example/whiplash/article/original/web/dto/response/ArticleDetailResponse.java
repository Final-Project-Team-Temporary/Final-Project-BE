package com.example.whiplash.article.original.web.dto.response;

import java.util.List;

import com.example.whiplash.article.summary.web.dto.response.ArticleSummaryDTO;

public record ArticleDetailResponse(
    List<ArticleSummaryDTO> summaries
) {}
