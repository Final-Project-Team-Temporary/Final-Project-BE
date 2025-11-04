package com.example.whiplash.article.summary.service;

import java.time.LocalDateTime;

public interface ArticleTaskProducer {
    String produce(String articleId, LocalDateTime timestamp);
}
