package com.example.whiplash.article.original.web.dto.response;

import com.example.whiplash.article.original.domain.document.SummaryStatus;

import java.time.LocalDateTime;

public record ArticleResponse(
    String id,
    String title,
    String content,
    LocalDateTime publishedAt,
    String url,
    String press,
    SummaryStatus summaryStatus
) {}
