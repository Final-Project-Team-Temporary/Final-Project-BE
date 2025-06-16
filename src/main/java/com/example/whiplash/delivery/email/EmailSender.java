package com.example.whiplash.delivery.email;

import java.util.List;

public interface EmailSender {
    void sendSummarizedArticle(Long userId, List<String> summarizedArticleIds);
}
