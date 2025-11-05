package com.example.whiplash.article.original.converter;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.original.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleResponse;
import com.example.whiplash.article.summary.web.dto.response.ArticleSummaryDTO;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;

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

    public static ArticleResponse toArticleResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getPublishedAt(),
                article.getUrl(),
                article.getPress(),
                article.getSummaryStatus()
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

    public static SummarizedArticleResponse toSummarizedArticleResponse(List<SummarizedArticle> summarizedArticles) {
        List<ArticleSummaryDTO> summaries = summarizedArticles.stream()
                .map(ArticleConverter::toArticleSummaryDTO)
                .collect(Collectors.toList());
        return new SummarizedArticleResponse(summaries);
    }

    @Deprecated
    public static ArticleDetailResponse toArticleDetailResponse(List<SummarizedArticle> summarizedArticles) {
        List<ArticleSummaryDTO> summaries = summarizedArticles.stream()
                .map(ArticleConverter::toArticleSummaryDTO)
                .collect(Collectors.toList());
        return new ArticleDetailResponse(summaries);
    }
}
