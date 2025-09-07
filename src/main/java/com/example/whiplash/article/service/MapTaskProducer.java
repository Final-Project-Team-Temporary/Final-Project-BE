package com.example.whiplash.article.service;

import com.example.whiplash.article.web.dto.response.SummarizationJobStatusDTO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapTaskProducer implements ArticleTaskProducer{
    private static final Map<String, SummarizationJobStatusDTO> jobStatusStorage = new ConcurrentHashMap<>();

    @Override
    public String produce(String articleId, LocalDateTime timestamp) {
        // 1-3. Redis 큐에 작업 등록 (현재는 임시 구현)
        String jobId = UUID.randomUUID().toString();

        // 임시 작업 상태 저장
        SummarizationJobStatusDTO jobStatus = SummarizationJobStatusDTO.builder()
                .jobId(jobId)
                .articleId(articleId)
                .status("ENQUEUED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attempts(0)
                .errorMessage(null)
                .build();

        jobStatusStorage.put(jobId, jobStatus);

        return jobId;
    }
}
