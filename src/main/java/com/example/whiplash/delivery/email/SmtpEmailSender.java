package com.example.whiplash.delivery.email;


import com.example.whiplash.article.document.SummarizedArticle;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.user.User;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final SummarizedArticleRepository summarizedArticleRepository;

    @Override
    public void sendSummarizedArticlesToUser(String email, List<String> summarizedArticleIds) {
        List<SummarizedArticle> articles = summarizedArticleRepository.findAllById(summarizedArticleIds);

        String content = transformSummaryToContent(articles);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("오늘의 요약 뉴스입니다");
        message.setText(content);

        mailSender.send(message);
    }

    private String transformSummaryToContent(List<SummarizedArticle> articles) {
        return articles.stream()
                .map(article -> String.format("!! %s\n%s\n", article.getTitle(), article.getSummarizedContent()))
                .collect(Collectors.joining("\n\n"));
    }
}
