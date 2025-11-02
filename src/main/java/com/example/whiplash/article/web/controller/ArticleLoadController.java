package com.example.whiplash.article.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.domain.document.SummaryStatus;
import com.example.whiplash.article.service.ArticleQueryService;
import com.example.whiplash.article.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.web.dto.response.ArticleListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleLoadController {

    private final ArticleQueryService articleQueryService;

    @GetMapping("/summarized")
    public ResponseEntity<ApiResponse<ArticleListResponse>> getArticleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<ArticleListItemResponse> articles = articleQueryService.getArticleList(pageable);
        ArticleListResponse response = ArticleListResponse.from(articles);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleDetail(
            @PathVariable String articleId) {

        ArticleDetailResponse articleDetail = articleQueryService.getArticleDetail(articleId);

        return ResponseEntity.ok(ApiResponse.onSuccess(articleDetail));
    }

    /**
     * 디버깅용: 전체 기사의 상태별 분포를 조회
     */
    @GetMapping("/debug/status-distribution")
    public ResponseEntity<ApiResponse<Map<SummaryStatus, Long>>> getArticleStatusDistribution() {
        Map<SummaryStatus, Long> distribution = articleQueryService.getArticleStatusDistribution();
        return ResponseEntity.ok(ApiResponse.onSuccess(distribution));
    }
}
