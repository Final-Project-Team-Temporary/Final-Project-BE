package com.example.whiplash.delivery.email;

import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmtpEmailSender implements EmailSender {

	private final JavaMailSender mailSender;
	private final SummarizedArticleRepository summarizedArticleRepository;
	private final TemplateEngine templateEngine;

	@Override
	public void sendSummarizedArticlesToUser(String email, List<String> summarizedArticleIds) {
		log.info("[SmtpEmailSender] 이메일 전송 진입 sendSummarizedArticlesToUser: email: {}, summarizedArticleIds: {}", email, summarizedArticleIds);
		List<SummarizedArticle> articles = summarizedArticleRepository.findAllById(summarizedArticleIds);

		Context context = new Context();
		context.setVariable("publishDate", LocalDate.now().toString());
		context.setVariable("editorName", "헤드위그");

		// 간단 요약 포인트
		List<String> highlights = articles.stream()
			.map(article -> article.getTitle())
			.toList();
		context.setVariable("highlights", highlights);

		// 기사 리스트 매핑
		List<Map<String, String>> mappedArticles = articles.stream()
			.map(a -> Map.of(
				"category", a.getCategory().name(),
				"title", a.getTitle(),
				"summary", a.getSummarizedContent(),
				"summaryLevel", a.getSummaryLevel().name(),
				"publishedAt", a.getPublishedAt().toLocalDate().toString()
			))
			.toList();

		context.setVariable("articles", mappedArticles);

		// HTML 렌더링
		String htmlContent = templateEngine.process("newsletter", context);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setTo(email);
			helper.setSubject("[Econoesay] 오늘의 요약 뉴스레터");
			helper.setText(htmlContent, true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("메일 전송 실패", e);
		}

		log.info("사용자 {}에게 ArticleAssignment: {}를 이메일로 전송했습니다", email,
			summarizedArticleIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
	}

}
