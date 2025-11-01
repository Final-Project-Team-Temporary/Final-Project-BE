package com.example.whiplash.article.web.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 기사 목록 페이징 응답 DTO
 * PageImpl 직렬화 경고를 해결하기 위한 안정적인 구조
 */
public record ArticleListResponse(
    List<ArticleListItemResponse> content,
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
     * Spring Data Page 객체를 ArticleListResponse로 변환
     */
    public static ArticleListResponse from(Page<ArticleListItemResponse> page) {
        return new ArticleListResponse(
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
