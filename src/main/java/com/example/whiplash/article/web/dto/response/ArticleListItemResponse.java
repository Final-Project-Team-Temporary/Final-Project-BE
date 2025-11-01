package com.example.whiplash.article.web.dto.response;

import java.time.LocalDateTime;

public record ArticleListItemResponse(
    String id,
    String title,
    LocalDateTime publishedAt
) {}
