package com.example.whiplash.article.summary.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.converter.ArticleConverter;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import com.example.whiplash.article.tag.repository.ArticleMatchedKeywordRepository;
import com.example.whiplash.article.tag.repository.ArticleStockRepository;
import com.example.whiplash.article.tag.service.ArticleEnrichmentService;
import com.example.whiplash.bookmark.entity.ArticleBookmark;
import com.example.whiplash.bookmark.repository.ArticleBookmarkRepository;
import com.example.whiplash.domain.entity.ArticleMatchedKeyword;
import com.example.whiplash.domain.entity.ArticleStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
