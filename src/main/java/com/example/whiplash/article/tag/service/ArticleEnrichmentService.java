package com.example.whiplash.article.tag.service;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.tag.repository.ArticleMatchedKeywordRepository;
import com.example.whiplash.article.tag.repository.ArticleStockRepository;
import com.example.whiplash.domain.entity.ArticleMatchedKeyword;
import com.example.whiplash.domain.entity.ArticleStock;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.client.dto.KeywordTermDto;
import com.example.whiplash.quiz.client.dto.StockDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleEnrichmentService {

    private final ArticleRepository articleRepository;
    private final ArticleMatchedKeywordRepository keywordRepository;
    private final ArticleStockRepository stockRepository;
    private final AiServerClient aiServerClient;

    /**
     * ⭐ 동기 방식 키워드/주식 추출 (배치용)
     */
    @Transactional
    public void enrichArticleSync(String articleId) {
        log.info("🔄 기사 보강 시작 (동기): articleId={}", articleId);

        try {
            // 1. 이미 처리된 경우 스킵
            if (hasKeywords(articleId) && hasStocks(articleId)) {
                log.info("⏭️ 이미 처리됨 - 스킵: articleId={}", articleId);
                return;
            }

            // 2. 기사 조회
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

            int keywordCount = 0;
            int stockCount = 0;

            // 3. 키워드 추출 (없는 경우만)
            if (!hasKeywords(articleId)) {
                keywordCount = extractAndSaveKeywords(article);
            }

            // 4. 주식 추출 (없는 경우만)
            if (!hasStocks(articleId)) {
                stockCount = extractAndSaveStocks(article);
            }

            log.info("✅ 기사 보강 완료 (동기): articleId={}, keywords={}, stocks={}",
                    articleId, keywordCount, stockCount);

        } catch (Exception e) {
            log.error("❌ 기사 보강 실패 (동기): articleId={}", articleId, e);
            throw new RuntimeException("기사 보강 실패", e);
        }
    }

    /**
     * ⭐ 비동기 방식 키워드/주식 추출 (실시간용)
     */
    @Async("articleEnrichmentExecutor")
    @Transactional
    public CompletableFuture<Void> enrichArticleAsync(String articleId) {
        log.info("🔄 기사 보강 시작 (비동기): articleId={}", articleId);

        try {
            enrichArticleSync(articleId);  // ⭐ 동기 메서드 재사용
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("❌ 기사 보강 실패 (비동기): articleId={}", articleId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 키워드 추출 및 저장
     */
    private int extractAndSaveKeywords(Article article) {
        log.debug("키워드 추출 시작: articleId={}", article.getId());

        List<KeywordTermDto> keywords =
                aiServerClient.extractKeywordsFromArticle(
                        article.getTitle(),
                        article.getContent()
                );

        for (KeywordTermDto keyword : keywords) {
            ArticleMatchedKeyword entity = ArticleMatchedKeyword.builder()
                    .articleId(article.getId())
                    .term(keyword.getTerm())
                    .termSummary(keyword.getTermSummary())
                    .build();

            keywordRepository.save(entity);
        }

        log.debug("키워드 저장 완료: articleId={}, count={}", article.getId(), keywords.size());
        return keywords.size();
    }

    /**
     * 주식 추출 및 저장
     */
    private int extractAndSaveStocks(Article article) {
        log.debug("주식 추출 시작: articleId={}", article.getId());

        List<StockDto> stocks =
                aiServerClient.extractStocksFromArticle(
                        article.getTitle(),
                        article.getContent()
                );

        for (StockDto stock : stocks) {
            ArticleStock entity = ArticleStock.builder()
                    .articleId(article.getId())
                    .stockName(stock.getStockName())
                    .stockCode(stock.getStockCode())
                    .market(stock.getMarket())
                    .sector(stock.getSector())
                    .build();

            stockRepository.save(entity);
        }

        log.debug("주식 저장 완료: articleId={}, count={}", article.getId(), stocks.size());
        return stocks.size();
    }

    /**
     * 키워드 존재 여부 확인
     */
    public boolean hasKeywords(String articleId) {
        return keywordRepository.existsByArticleId(articleId);
    }

    /**
     * 주식 존재 여부 확인
     */
    public boolean hasStocks(String articleId) {
        return stockRepository.existsByArticleId(articleId);
    }

    /**
     * 기사 키워드 조회
     */
    @Transactional(readOnly = true)
    public List<ArticleMatchedKeyword> getKeywordsByArticleId(String articleId) {
        return keywordRepository.findByArticleId(articleId);
    }

    /**
     * 기사 주식 조회
     */
    @Transactional(readOnly = true)
    public List<ArticleStock> getStocksByArticleId(String articleId) {
        return stockRepository.findByArticleId(articleId);
    }
}
