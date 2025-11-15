package com.example.whiplash.recommend.article.web.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 기사 추천 목록 페이징 응답 DTO
 */
public record ArticleRecommendResponse(
	List<ArticleRecommendItemResponse> content,
	int currentPage,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext,
	boolean hasPrevious,
	boolean isFirst,
	boolean isLast
) {
	/**
	 * Spring Data Page 객체를 ArticleRecommendResponse로 변환
	 */
	public static ArticleRecommendResponse from(Page<ArticleRecommendItemResponse> page) {
		return new ArticleRecommendResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext(),
			page.hasPrevious(),
			page.isFirst(),
			page.isLast()
		);
	}
}
