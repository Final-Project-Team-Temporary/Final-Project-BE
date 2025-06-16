package com.example.whiplash.delivery.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class EmailSendingService {
    private final EmailSender emailSender;

    public void sendSummarizedArticlesToUser(Long userId, List<String> summarizedArticleIds) {
        emailSender.sendSummarizedArticle(userId, summarizedArticleIds);
    }


}
