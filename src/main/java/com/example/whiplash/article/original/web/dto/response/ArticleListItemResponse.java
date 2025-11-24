package com.example.whiplash.article.original.web.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ArticleListItemResponse(
    String id,
    String title,
    LocalDateTime publishedAt,
    Boolean isBookmarked,
    Long bookmarkId
) {}
