package com.example.whiplash.article.summary.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.summary.service.SummarizedArticleQueryService;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summarized-articles")
public class SummarizedArticleController {

    private final SummarizedArticleQueryService summarizedArticleQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<SummarizedArticleResponse>> getSummarizedArticles(
            @RequestParam String originalArticleId) {

        SummarizedArticleResponse response = summarizedArticleQueryService
                .getSummarizedArticles(originalArticleId);

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
