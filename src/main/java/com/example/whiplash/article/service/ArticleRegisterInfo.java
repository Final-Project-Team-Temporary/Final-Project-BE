package com.example.whiplash.article.service;

import java.util.ArrayList;
import java.util.List;

public record ArticleRegisterInfo(
        List<String> processedArticleIds,
        List<String> failedIds ,
        List<String> jobIds
) {
    public ArticleRegisterInfo {
        if (processedArticleIds == null) {
            processedArticleIds = new ArrayList<>();
        }
        if (failedIds == null) {
            failedIds = new ArrayList<>();
        }
        if (jobIds == null) {
            jobIds = new ArrayList<>();
        }
    }
}
