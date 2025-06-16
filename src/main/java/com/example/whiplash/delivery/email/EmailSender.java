package com.example.whiplash.delivery.email;

import java.util.List;

public interface EmailSender {
    void sendSummarizedArticlesToUser(String email, List<String> summarizedArticleIds);
}
