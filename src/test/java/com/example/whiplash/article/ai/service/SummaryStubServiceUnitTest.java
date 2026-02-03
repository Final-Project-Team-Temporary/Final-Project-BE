/*
package com.example.whiplash.article.ai.service;

import com.example.whiplash.article.ai.dto.AiSummarizationRequest;
import com.example.whiplash.article.ai.dto.AiSummarizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SummaryStubService 단위 테스트")
class SummaryStubServiceUnitTest {

    private SummaryStubService summaryStubService;

    @BeforeEach
    void setUp() {
        summaryStubService = new SummaryStubService();
        
        // 테스트를 위한 설정값 주입
        ReflectionTestUtils.setField(summaryStubService, "successRate", 0.8);
        ReflectionTestUtils.setField(summaryStubService, "minDelaySeconds", 1);
        ReflectionTestUtils.setField(summaryStubService, "maxDelaySeconds", 2);
    }

    @Test
    @DisplayName("요약 요청이 성공적으로 처리되어야 한다")
    void should_processRequest_when_requestSummarization() throws ExecutionException, InterruptedException {
        // given
        AiSummarizationRequest request = new AiSummarizationRequest(
                "test-article-1",
                "테스트 기사 내용입니다. 이것은 AI 서버 스터빙을 위한 테스트 데이터입니다.",
                "https://example.com/test-article-1",
                300
        );

        // when
        CompletableFuture<AiSummarizationResponse> future = summaryStubService.requestSummarization(request);
        AiSummarizationResponse response = future.get();

        // then
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.jobId()).isNotBlank();
                    assertThat(res.articleId()).isEqualTo("test-article-1");
                    assertThat(res.status()).isEqualTo("PROCESSING");
                    assertThat(res.success()).isTrue();
                });
    }

    @Test
    @DisplayName("작업 상태 조회가 정상적으로 동작해야 한다")
    void should_returnJobStatus_when_getSummarizationStatus() throws ExecutionException, InterruptedException {
        // given
        AiSummarizationRequest request = new AiSummarizationRequest(
                "test-article-2",
                "상태 조회 테스트를 위한 기사 내용입니다.",
                "https://example.com/test-article-2",
                300
        );
        
        // 먼저 요약 요청
        CompletableFuture<AiSummarizationResponse> requestFuture = summaryStubService.requestSummarization(request);
        AiSummarizationResponse requestResponse = requestFuture.get();
        String jobId = requestResponse.jobId();

        // when
        CompletableFuture<AiSummarizationResponse> statusFuture = summaryStubService.getSummarizationStatus(jobId);
        AiSummarizationResponse statusResponse = statusFuture.get();

        // then
        assertThat(statusResponse)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.jobId()).isEqualTo(jobId);
                    assertThat(res.articleId()).isEqualTo("test-article-2");
                    assertThat(res.status()).isIn("PROCESSING", "COMPLETED", "FAILED");
                });
    }

    @Test
    @DisplayName("존재하지 않는 작업 ID 조회 시 적절한 오류 응답을 반환해야 한다")
    void should_returnNotFoundResponse_when_nonExistentJobId() throws ExecutionException, InterruptedException {
        // given
        String nonExistentJobId = "non-existent-job-id";

        // when
        CompletableFuture<AiSummarizationResponse> future = summaryStubService.getSummarizationStatus(nonExistentJobId);
        AiSummarizationResponse response = future.get();

        // then
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.jobId()).isEqualTo(nonExistentJobId);
                    assertThat(res.articleId()).isEqualTo("unknown");
                    assertThat(res.success()).isFalse();
                    assertThat(res.errorMessage()).contains("작업을 찾을 수 없습니다");
                });
    }

    @Test
    @DisplayName("SummaryStubService가 프로파일 어노테이션을 올바르게 사용하는지 확인")
    void should_haveCorrected_profileAnnotation() {
        // given
        Class<SummaryStubService> clazz = SummaryStubService.class;
        
        // when
        org.springframework.context.annotation.Profile profileAnnotation = 
                clazz.getAnnotation(org.springframework.context.annotation.Profile.class);

        // then
        assertThat(profileAnnotation)
                .isNotNull();
        
        assertThat(profileAnnotation.value())
                .contains("dev", "!prod");
    }
}*/
