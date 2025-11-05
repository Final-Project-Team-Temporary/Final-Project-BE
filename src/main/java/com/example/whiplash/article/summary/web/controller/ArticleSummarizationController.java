package com.example.whiplash.article.summary.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.summary.web.dto.request.ArticleSummarizationRequest;
import com.example.whiplash.article.summary.web.dto.response.ArticleSummarizationResponse;
import com.example.whiplash.article.summary.web.dto.response.SummarizationJobStatusDTO;
import com.example.whiplash.article.summary.service.ArticleSummarizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/articles/summarization")
@RestController
public class ArticleSummarizationController {

    private final ArticleSummarizationService articleSummarizationService;
    
    @PostMapping("/request")
    public ApiResponse<ArticleSummarizationResponse> requestSummarization(
            @Valid @RequestBody ArticleSummarizationRequest request) {
        
        log.info("크롤러 요약 요청 수신: 요청된 기사 개수={}",
                request.getArticleIds().size());
        
        ArticleSummarizationResponse response = articleSummarizationService.processArticleSummarizationRequest(request);
        
        log.info("요약 요청 처리 완료: processedCount={}", response.getProcessedCount());
        
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/status/{jobId}")
    public ApiResponse<SummarizationJobStatusDTO> getJobStatus(@PathVariable String jobId) {
        
        log.info("작업 상태 조회 요청: jobId={}", jobId);
        
        SummarizationJobStatusDTO statusDto = articleSummarizationService.getJobStatus(jobId);
        
        log.info("작업 상태 조회 완료: jobId={}, status={}", jobId, statusDto.getStatus());
        
        return ApiResponse.onSuccess(statusDto);
    }
}