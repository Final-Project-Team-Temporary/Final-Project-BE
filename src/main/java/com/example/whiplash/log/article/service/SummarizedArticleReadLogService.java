package com.example.whiplash.log.article.service;

import static org.springframework.transaction.annotation.Propagation.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.log.article.entity.SummarizedArticleReadLog;
import com.example.whiplash.log.article.repository.SummarizedArticleReadLogRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SummarizedArticleReadLogService {

	private final SummarizedArticleReadLogRepository summarizedArticleReadLogRepository;

	@Transactional(propagation = REQUIRES_NEW)
	public Long saveSummarizedArticleReadLog(Long userId, String articleId, LocalDateTime readAt) {
		SummarizedArticleReadLog log = SummarizedArticleReadLog.builder()
			.userId(userId)
			.articleId(articleId)
			.readAt(readAt)
			.build();

		SummarizedArticleReadLog savedLog = summarizedArticleReadLogRepository.save(log);
		return savedLog.getId();
	}
}
