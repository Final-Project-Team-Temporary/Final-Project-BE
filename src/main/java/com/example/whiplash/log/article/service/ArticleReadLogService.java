package com.example.whiplash.log.article.service;

import static org.springframework.transaction.annotation.Propagation.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.log.article.entity.ArticleReadLog;
import com.example.whiplash.log.article.repository.ArticleReadLogRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleReadLogService {

	private final ArticleReadLogRepository articleReadLogRepository;

	@Transactional(propagation = REQUIRES_NEW)
	public Long saveArticleReadLog(Long userId, String articleId, LocalDateTime readAt) {
		ArticleReadLog log = ArticleReadLog.builder()
			.userId(userId)
			.articleId(articleId)
			.readAt(readAt)
			.build();

		ArticleReadLog savedLog = articleReadLogRepository.save(log);
		return savedLog.getId();
	}
}
