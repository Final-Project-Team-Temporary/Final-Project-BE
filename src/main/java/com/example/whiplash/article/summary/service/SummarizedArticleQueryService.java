package com.example.whiplash.article.summary.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.converter.ArticleConverter;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummarizedArticleQueryService {

    private final ArticleRepository articleRepository;
    private final SummarizedArticleRepository summarizedArticleRepository;

    public SummarizedArticleResponse getSummarizedArticles(String originalArticleId) {
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

        return ArticleConverter.toSummarizedArticleResponse(summarizedArticles);
    }
}
