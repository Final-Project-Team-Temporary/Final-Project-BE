package com.example.whiplash.delivery.email;


import com.example.whiplash.article.document.SummarizedArticle;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.user.User;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("사용자 {}에게 ArticleAssignment: {}를 이메일로 전송했습니다", email, summarizedArticleIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    private String transformSummaryToContent(List<SummarizedArticle> articles) {
        return articles.stream()
                .map(article -> String.format("!! %s\n%s\n", article.getTitle(), article.getSummarizedContent()))
                .collect(Collectors.joining("\n\n"));
    }
}
