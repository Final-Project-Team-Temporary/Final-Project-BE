package com.example.whiplash.article.converter;

import com.example.whiplash.article.domain.document.Article;
import com.example.whiplash.article.domain.document.SummarizedArticle;
import com.example.whiplash.article.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.web.dto.response.ArticleSummaryDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ArticleConverter {

    public static ArticleListItemResponse toArticleListItemResponse(Article article) {
        return new ArticleListItemResponse(
                article.getId(),
                article.getTitle(),
                article.getPublishedAt()
        );
    }

    public static ArticleSummaryDTO toArticleSummaryDTO(SummarizedArticle summarizedArticle) {
        return new ArticleSummaryDTO(
                summarizedArticle.getTitle(),
                summarizedArticle.getCategory(),
                summarizedArticle.getSummarizedContent(),
                summarizedArticle.getSummaryLevel(),
                summarizedArticle.getPublishedAt()
        );
    }

    public static ArticleDetailResponse toArticleDetailResponse(List<SummarizedArticle> summarizedArticles) {
        List<ArticleSummaryDTO> summaries = summarizedArticles.stream()
                .map(ArticleConverter::toArticleSummaryDTO)
                .collect(Collectors.toList());
        return new ArticleDetailResponse(summaries);
    }
}
