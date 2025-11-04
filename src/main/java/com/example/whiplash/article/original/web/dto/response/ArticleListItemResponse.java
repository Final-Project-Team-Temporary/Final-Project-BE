package com.example.whiplash.article.original.web.dto.response;

import java.time.LocalDateTime;

public record ArticleListItemResponse(
    String id,
    String title,
    LocalDateTime publishedAt
) {}
