package com.example.whiplash.article.service;

import com.example.whiplash.article.domain.document.Article;
import com.example.whiplash.article.domain.document.SummaryStatus;
import com.example.whiplash.article.repository.ArticleRepository;
import com.example.whiplash.article.web.dto.request.ArticleSummarizationRequest;
import com.example.whiplash.article.web.dto.response.ArticleSummarizationResponse;
import com.example.whiplash.article.web.dto.response.SummarizationJobStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArticleSummarizationService {

    private final ArticleRepository articleRepository;

    private final ArticleTaskProducer articleTaskProducer;

    @Transactional
    public ArticleSummarizationResponse processArticleSummarizationRequest(ArticleSummarizationRequest request) {
        log.info("크롤러 메타데이터 포함 요약 요청 처리 시작");

        ArticleRegisterInfo registerInfo = new ArticleRegisterInfo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        // 1. 작업 큐 등록
        for (String articleId : request.getArticleIds()) {
            registerArticleToTaskQueue(articleId, registerInfo, LocalDateTime.now());
        }

        // 2. 응답 생성
        boolean success = !registerInfo.processedArticleIds().isEmpty();

        ArticleSummarizationResponse response = ArticleSummarizationResponse.create(
                success,
                registerInfo,
                String.format("총 %d개 중 %d개 성공적으로 처리됨 (메타데이터 저장 후 큐 등록)",
                        request.getArticleIds().size(), registerInfo.processedArticleIds().size()));

        log.info("크롤러 메타데이터 포함 요약 요청 처리 완료: processed={}, failed={}",
                registerInfo.processedArticleIds().size(), registerInfo.failedIds().size());

        return response;
    }

    private void registerArticleToTaskQueue(String articleId, ArticleRegisterInfo registerInfo, LocalDateTime registeredAt) {
        List<String> processedArticleIds = registerInfo.processedArticleIds();
        List<String> failedIds = registerInfo.failedIds();
        List<String> jobIds = registerInfo.jobIds();
        try {
            // 1-2. MongoDB Article 존재 여부 확인 및 상태 업데이트
            Optional<Article> articleOpt = articleRepository.findById(articleId);

            if (articleOpt.isPresent()) {
                Article article = articleOpt.get();

                // Article 상태를 ENQUEUED로 업데이트
                article.setSummaryStatus(SummaryStatus.ENQUEUED);
                articleRepository.save(article);

                String jobId = articleTaskProducer.produce(articleId, registeredAt);

                processedArticleIds.add(articleId);
                jobIds.add(jobId);

                log.debug("Article 상태 업데이트 및 큐 등록 완료: articleId={}, jobId={}", articleId, jobId);
            } else {
                log.warn("존재하지 않는 Article ID (메타데이터는 저장됨): {}", articleId);
                // 메타데이터는 저장되었지만 MongoDB Article이 없는 경우
                // 나중에 Article이 생성될 때를 대비해 메타데이터는 유지
                failedIds.add(articleId);
            }

        } catch (Exception e) {
            log.error("Article 메타데이터 처리 중 오류 발생: articleId={}, error={}", articleId, e.getMessage());
            failedIds.add(articleId);
        }
    }

    // TODO: not finished yet
    public SummarizationJobStatusDTO getJobStatus(String jobId) {
        log.info("작업 상태 조회: jobId={}", jobId);

        SummarizationJobStatusDTO status = null;

        if (status == null) {
            log.warn("존재하지 않는 작업 ID: {}", jobId);
            // 존재하지 않는 job에 대한 기본 응답
            throw new IllegalArgumentException("존재하지 않는 작업입니다");
        }

        log.info("작업 상태 조회 완료: jobId={}, status={}", jobId, status.getStatus());
        return status;
    }

}