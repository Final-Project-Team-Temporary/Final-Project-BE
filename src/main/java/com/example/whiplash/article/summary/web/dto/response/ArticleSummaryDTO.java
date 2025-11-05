package com.example.whiplash.article.summary.web.dto.response;

import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;

import java.time.LocalDateTime;

public record ArticleSummaryDTO(
    String title,
    Category category,
    String summarizedContent,
    SummaryLevel summaryLevel,
    LocalDateTime publishedAt
) {}
