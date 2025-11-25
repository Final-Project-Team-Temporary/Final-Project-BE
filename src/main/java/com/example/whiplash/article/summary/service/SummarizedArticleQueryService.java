package com.example.whiplash.article.summary.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.converter.ArticleConverter;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleReadRedisRepository;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import com.example.whiplash.article.tag.service.ArticleEnrichmentService;
import com.example.whiplash.bookmark.entity.ArticleBookmark;
import com.example.whiplash.bookmark.repository.ArticleBookmarkRepository;
import com.example.whiplash.daily.learning.entity.LearningType;
import com.example.whiplash.daily.learning.event.LearningPerformedEvent;
import com.example.whiplash.domain.entity.ArticleMatchedKeyword;
import com.example.whiplash.domain.entity.ArticleStock;
import com.example.whiplash.global.event.DomainEventPublisher;
import com.example.whiplash.log.article.service.SummarizedArticleReadLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummarizedArticleQueryService {

	private final ArticleRepository articleRepository;
	private final SummarizedArticleRepository summarizedArticleRepository;
	private final ArticleEnrichmentService articleEnrichmentService;
	private final ArticleBookmarkRepository bookmarkRepository;
	private final ArticleReadRedisRepository articleReadRedisRepository;
	private final DomainEventPublisher eventPublisher;
	private final SummarizedArticleReadLogService summarizedArticleReadLogService;

	public SummarizedArticleResponse getArticlesAndPublishEvent(Optional<Long> userIdOpt,
		String originalArticleId,
		LocalDateTime now
	) {
		if (!userIdOpt.isEmpty()) {
			Long userId = userIdOpt.get();

			publishEventIfFirstReadArticle(originalArticleId, userId, now);

			recordSummarizedArticleReadLog(originalArticleId, userId, now);
		}
		return getSummarizedArticles(userIdOpt.get(), originalArticleId);
	}

	public SummarizedArticleResponse getSummarizedArticles(Long userId, String originalArticleId) {
		// Article 존재 여부 확인
		Article article = articleRepository.findById(originalArticleId)
			.orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

		// originalArticleId로 요약된 기사들 조회
		List<SummarizedArticle> summarizedArticles = summarizedArticleRepository
			.findAllByOriginalArticleId(originalArticleId);

		// 요약된 기사가 없으면 예외
		if (summarizedArticles.isEmpty()) {
			throw new WhiplashException(ErrorStatus.SUMMARIZED_ARTICLE_NOT_FOUND);
		}

		// 3개의 난이도가 모두 있는지 검증 (선택적)
		if (summarizedArticles.size() != 3) {
			throw new WhiplashException(ErrorStatus.INCOMPLETE_ARTICLE_SUMMARIES);
		}

		String articleId = article.getId();

		List<ArticleMatchedKeyword> keywords = articleEnrichmentService.getKeywordsByArticleId(articleId);
		List<ArticleStock> stocks = articleEnrichmentService.getStocksByArticleId(articleId);
		Optional<ArticleBookmark> bookmark = bookmarkRepository.findByUserIdAndArticleId(userId, articleId);

		// 3. ⭐ 키워드/주식이 없으면 비동기로 생성 시작
		boolean hasKeywords = !keywords.isEmpty();
		boolean hasStocks = !stocks.isEmpty();

		boolean isBookmarked = bookmark.isPresent();

		if (!hasKeywords || !hasStocks) {
			log.warn("⚠️ 태그 없음 - 비동기 생성 시작: articleId={}, hasKeywords={}, hasStocks={}",
				articleId, hasKeywords, hasStocks);

			articleEnrichmentService.enrichArticleAsync(articleId);
		}

		return ArticleConverter.toSummarizedArticleResponse(summarizedArticles, keywords, stocks, isBookmarked);
	}

	private void publishEventIfFirstReadArticle(String articleId, Long userId, LocalDateTime now) {
		if (!articleReadRedisRepository.isRead(userId, articleId)) {
			// 처음 읽는 경우
			eventPublisher.publish(
				new LearningPerformedEvent(userId, now, LearningType.ARTICLE)
			);

			// 2. Redis에 읽음 표시 (오늘 하루 유효)
			articleReadRedisRepository.readArticle(userId, articleId);

			log.info("✅ 사용자 {}가 기사 {}를 처음 읽음 - 이벤트 발행 완료", userId, articleId);
		}
	}

	private void recordSummarizedArticleReadLog(String articleId, Long userId, LocalDateTime readAt) {
		Long id = summarizedArticleReadLogService.saveSummarizedArticleReadLog(userId, articleId, readAt);
		log.info("요약 기사 읽기 로그 저장됨 아이디: {}", id);
	}
}
