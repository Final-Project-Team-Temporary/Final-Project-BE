package com.example.whiplash.recommend.article.web.dto.response;

import java.time.LocalDateTime;

import com.example.whiplash.article.original.domain.document.Article;

/**
 * 추천 기사 항목 DTO
 */
public record ArticleRecommendItemResponse(
	String id,
	String title,
	String press,
	String url,
	LocalDateTime publishedAt
) {
	/**
	 * Article 엔티티를 ArticleRecommendItemDTO로 변환
	 */
	public static ArticleRecommendItemResponse from(Article article) {
		return new ArticleRecommendItemResponse(
			article.getId(),
			article.getTitle(),
			article.getPress(),
			article.getUrl(),
			article.getPublishedAt()
		);
	}
}
