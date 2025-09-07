package com.example.whiplash.article.service;

import java.time.LocalDateTime;

public interface ArticleTaskProducer {
    String produce(String articleId, LocalDateTime timestamp);
}
