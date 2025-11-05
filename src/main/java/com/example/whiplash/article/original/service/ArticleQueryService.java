package com.example.whiplash.article.original.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.converter.ArticleConverter;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.original.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleQueryService {

    private final ArticleRepository articleRepository;
    private final SummarizedArticleRepository summarizedArticleRepository;

    public Page<ArticleListItemResponse> getArticleList(Pageable pageable) {
        Page<Article> articles = articleRepository.findBySummaryStatus(SummaryStatus.COMPLETED, pageable);
        return articles.map(ArticleConverter::toArticleListItemResponse);
    }

    public ArticleResponse getOriginalArticle(String articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

        return ArticleConverter.toArticleResponse(article);
    }

    public ArticleDetailResponse getArticleDetail(String articleId) {
        // Article 존재 여부 확인
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

        // originalArticleId로 요약된 기사들 조회
        List<SummarizedArticle> summarizedArticles = summarizedArticleRepository
                .findAllByOriginalArticleId(articleId);

        // 요약된 기사가 없으면 예외
        if (summarizedArticles.isEmpty()) {
            throw new WhiplashException(ErrorStatus.SUMMARIZED_ARTICLE_NOT_FOUND);
        }

        // 3개의 난이도가 모두 있는지 검증 (선택적)
        if (summarizedArticles.size() != 3) {
            throw new WhiplashException(ErrorStatus.INCOMPLETE_ARTICLE_SUMMARIES);
        }

        return ArticleConverter.toArticleDetailResponse(summarizedArticles);
    }

    /**
     * 디버깅용: 전체 기사의 상태별 분포를 조회
     */
    public Map<SummaryStatus, Long> getArticleStatusDistribution() {
        List<Article> allArticles = articleRepository.findAll();
        return allArticles.stream()
                .collect(Collectors.groupingBy(Article::getSummaryStatus, Collectors.counting()));
    }
}
