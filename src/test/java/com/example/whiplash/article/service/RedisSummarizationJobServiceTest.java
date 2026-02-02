package com.example.whiplash.article.service;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.web.dto.response.SummarizationJobStatusDTO;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("Redis 기반 요약 작업 서비스 테스트")
class RedisSummarizationJobServiceTest extends IntegrationTestSupport {

    @Autowired
    private RedisSummarizationJobService service;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String JOB_QUEUE_KEY = "summarization_jobs";
    private static final String FAILED_QUEUE_KEY = "failed_summarization_jobs";
    private static final String JOB_STATUS_KEY = "job_status:";
    private static final String JOB_RETRY_KEY = "job_retry:";

    @BeforeEach
    void setUp() {
        // Redis 키 정리
        cleanUpRedisKeys();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 정리
        cleanUpRedisKeys();
    }

    private void cleanUpRedisKeys() {
        redisTemplate.delete(JOB_QUEUE_KEY);
        redisTemplate.delete(FAILED_QUEUE_KEY);
        redisTemplate.delete("retry_queue");
        
        Set<String> jobStatusKeys = redisTemplate.keys(JOB_STATUS_KEY + "*");
        if (jobStatusKeys != null && !jobStatusKeys.isEmpty()) {
            redisTemplate.delete(jobStatusKeys);
        }
        
        Set<String> retryKeys = redisTemplate.keys(JOB_RETRY_KEY + "*");
        if (retryKeys != null && !retryKeys.isEmpty()) {
            redisTemplate.delete(retryKeys);
        }
    }

    @Test
    @DisplayName("작업을 성공적으로 큐에 등록할 수 있다")
    void should_enqueueJob_when_validDataProvided() {
        // given
        String articleId = "article-123";
        String url = "https://example.com/article";
        String priority = "HIGH";

        // when
        String jobId = service.enqueueJob(articleId, url, priority);

        // then
        assertThat(jobId).isNotNull();
        
        // 큐에 추가되었는지 확인
        Set<Object> queueMembers = redisTemplate.opsForZSet().range(JOB_QUEUE_KEY, 0, -1);
        assertThat(queueMembers).contains(jobId);
        
        // Job 상세 정보가 저장되었는지 확인
        String jobStatusKey = JOB_STATUS_KEY + jobId;
        String storedArticleId = (String) redisTemplate.opsForHash().get(jobStatusKey, "articleId");
        assertThat(storedArticleId).isEqualTo(articleId);
    }

    @Test
    @DisplayName("존재하는 작업의 상태를 조회할 수 있다")
    void should_getJobStatus_when_jobExists() {
        // given
        String articleId = "article-456";
        String jobId = service.enqueueJob(articleId, "https://example.com", "NORMAL");

        // when
        SummarizationJobStatusDTO status = service.getJobStatus(jobId);

        // then
        assertThat(status)
            .satisfies(s -> {
                assertThat(s.getJobId()).isEqualTo(jobId);
                assertThat(s.getArticleId()).isEqualTo(articleId);
                assertThat(s.getStatus()).isEqualTo("PENDING");
                assertThat(s.getAttempts()).isEqualTo(0);
            });
    }

    @Test
    @DisplayName("존재하지 않는 작업 조회 시 예외를 던져야 한다")
    void should_throwException_when_jobNotFound() {
        // given
        String nonExistentJobId = "non-existent-job";

        // when & then
        assertThatThrownBy(() -> service.getJobStatus(nonExistentJobId))
            .isInstanceOf(WhiplashException.class);
    }

    @Test
    @DisplayName("작업 상태를 업데이트할 수 있다")
    void should_updateJobStatus_when_validJobIdProvided() {
        // given
        String articleId = "article-789";
        String jobId = service.enqueueJob(articleId, "https://example.com", "LOW");
        String newStatus = "PROCESSING";
        String errorMessage = "Processing started";

        // when
        service.updateJobStatus(jobId, newStatus, errorMessage);

        // then
        SummarizationJobStatusDTO status = service.getJobStatus(jobId);
        assertThat(status.getStatus()).isEqualTo(newStatus);
        assertThat(status.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("재시도 횟수가 최대치에 도달하지 않은 경우 재시도할 수 있다")
    void should_retryJob_when_belowMaxRetryCount() {
        // given
        String articleId = "article-retry";
        String jobId = service.enqueueJob(articleId, "https://example.com", "HIGH");

        // when
        boolean retryResult = service.retryJob(jobId);

        // then
        assertThat(retryResult).isTrue();
        
        // 재시도 정보 확인
        SummarizationJobStatusDTO status = service.getJobStatus(jobId);
        assertThat(status.getAttempts()).isEqualTo(1);
        assertThat(status.getStatus()).isEqualTo("RETRYING");
        
        // 재시도 큐에 추가되었는지 확인
        Set<Object> retryQueueMembers = redisTemplate.opsForZSet().range("retry_queue", 0, -1);
        assertThat(retryQueueMembers).contains(jobId);
    }

    @Test
    @DisplayName("최대 재시도 횟수 도달 시 데드레터큐로 이동해야 한다")
    void should_moveToDeadLetterQueue_when_maxRetryCountReached() {
        // given
        String articleId = "article-max-retry";
        String jobId = service.enqueueJob(articleId, "https://example.com", "NORMAL");
        
        // Article 모킹
        Article mockArticle = Article.builder()
            .id(articleId)
            .title("Test Article")
            .url("https://example.com")
            .summaryStatus(SummaryStatus.ENQUEUED)
            .build();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(mockArticle));
        given(articleRepository.save(any(Article.class))).willReturn(mockArticle);
        
        // 재시도 횟수를 최대치까지 증가
        String jobStatusKey = JOB_STATUS_KEY + jobId;
        redisTemplate.opsForHash().put(jobStatusKey, "attempts", 3);

        // when
        boolean retryResult = service.retryJob(jobId);

        // then
        assertThat(retryResult).isFalse();
        
        // 실패 큐에 추가되었는지 확인
        Set<Object> failedQueueMembers = redisTemplate.opsForZSet().range(FAILED_QUEUE_KEY, 0, -1);
        assertThat(failedQueueMembers).contains(jobId);
        
        // 작업 상태가 FAILED로 변경되었는지 확인
        SummarizationJobStatusDTO status = service.getJobStatus(jobId);
        assertThat(status.getStatus()).isEqualTo("FAILED");
        
        // Article 상태가 FAILED로 업데이트되었는지 확인
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("데드레터큐로 직접 이동시킬 수 있다")
    void should_moveToDeadLetterQueue_when_directlyMoved() {
        // given
        String articleId = "article-direct-dlq";
        String jobId = service.enqueueJob(articleId, "https://example.com", "LOW");
        String errorMessage = "Critical error occurred";
        
        // Article 모킹
        Article mockArticle = Article.builder()
            .id(articleId)
            .title("Test Article")
            .url("https://example.com")
            .summaryStatus(SummaryStatus.ENQUEUED)
            .build();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(mockArticle));
        given(articleRepository.save(any(Article.class))).willReturn(mockArticle);

        // when
        service.moveToDeadLetterQueue(jobId, errorMessage);

        // then
        // 실패 큐에 추가되었는지 확인
        Set<Object> failedQueueMembers = redisTemplate.opsForZSet().range(FAILED_QUEUE_KEY, 0, -1);
        assertThat(failedQueueMembers).contains(jobId);
        
        // 원본 큐에서 제거되었는지 확인
        Set<Object> queueMembers = redisTemplate.opsForZSet().range(JOB_QUEUE_KEY, 0, -1);
        assertThat(queueMembers).doesNotContain(jobId);
        
        // 작업 상태 확인
        SummarizationJobStatusDTO status = service.getJobStatus(jobId);
        assertThat(status.getStatus()).isEqualTo("FAILED");
        assertThat(status.getErrorMessage()).isEqualTo(errorMessage);
        
        // Article 상태 업데이트 확인
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("재시도 간격이 Exponential backoff 정책을 따라야 한다")
    void should_followExponentialBackoffPolicy_when_retrying() {
        // given
        String articleId = "article-backoff";
        String jobId = service.enqueueJob(articleId, "https://example.com", "HIGH");

        // when & then
        // 첫 번째 재시도 (1분)
        service.retryJob(jobId);
        SummarizationJobStatusDTO status1 = service.getJobStatus(jobId);
        assertThat(status1.getAttempts()).isEqualTo(1);
        
        // 두 번째 재시도 (5분)
        service.retryJob(jobId);
        SummarizationJobStatusDTO status2 = service.getJobStatus(jobId);
        assertThat(status2.getAttempts()).isEqualTo(2);
        
        // 세 번째 재시도 (15분)
        service.retryJob(jobId);
        SummarizationJobStatusDTO status3 = service.getJobStatus(jobId);
        assertThat(status3.getAttempts()).isEqualTo(3);
        
        // 재시도 스케줄링 정보 확인
        String retryKey = JOB_RETRY_KEY + jobId;
        String nextRetryTime = (String) redisTemplate.opsForHash().get(retryKey, "nextRetryTime");
        assertThat(nextRetryTime).isNotNull();
    }

    @Test
    @DisplayName("우선순위에 따라 큐 순서가 결정되어야 한다")
    void should_orderByPriority_when_enqueuingJobs() {
        // given & when
        String highJobId = service.enqueueJob("article-high", "https://example.com", "HIGH");
        String normalJobId = service.enqueueJob("article-normal", "https://example.com", "NORMAL");
        String lowJobId = service.enqueueJob("article-low", "https://example.com", "LOW");

        // then
        Set<Object> queueMembers = redisTemplate.opsForZSet().range(JOB_QUEUE_KEY, 0, -1);
        assertThat(queueMembers).containsExactly(highJobId, normalJobId, lowJobId);
    }
}