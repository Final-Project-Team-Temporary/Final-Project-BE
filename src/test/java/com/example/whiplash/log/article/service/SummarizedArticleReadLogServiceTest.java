package com.example.whiplash.log.article.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.log.article.entity.SummarizedArticleReadLog;
import com.example.whiplash.log.article.repository.SummarizedArticleReadLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("요약 기사 읽기 로그 서비스 테스트")
class SummarizedArticleReadLogServiceTest extends IntegrationTestSupport {

	@Autowired
	SummarizedArticleReadLogService summarizedArticleReadLogService;

	@Autowired
	SummarizedArticleReadLogRepository summarizedArticleReadLogRepository;

	@AfterEach
	void tearDown() {
		summarizedArticleReadLogRepository.deleteAll();
	}

	@Test
	@DisplayName("요약 기사 읽기 로그를 저장할 수 있다")
	void should_saveLog_when_validDataProvided() {
		// given
		Long userId = 1L;
		String articleId = "test-article-id";
		LocalDateTime readAt = LocalDateTime.now();

		// when
		Long savedLogId = summarizedArticleReadLogService.saveSummarizedArticleReadLog(userId, articleId, readAt);

		// then
		assertThat(savedLogId).isNotNull();

		SummarizedArticleReadLog savedLog = summarizedArticleReadLogRepository.findById(savedLogId)
			.orElseThrow();

		assertThat(savedLog)
			.satisfies(log -> {
				assertThat(log.getId()).isEqualTo(savedLogId);
				assertThat(log.getUserId()).isEqualTo(userId);
				assertThat(log.getArticleId()).isEqualTo(articleId);
				assertThat(log.getReadAt()).isEqualTo(readAt);
			});
	}

	@Test
	@DisplayName("동일 사용자가 같은 기사를 여러 번 읽으면 각각 로그가 저장된다")
	void should_saveMultipleLogs_when_sameUserReadsSameArticleMultipleTimes() {
		// given
		Long userId = 1L;
		String articleId = "test-article-id";
		LocalDateTime firstReadAt = LocalDateTime.now().minusHours(2);
		LocalDateTime secondReadAt = LocalDateTime.now();

		// when
		Long firstLogId = summarizedArticleReadLogService.saveSummarizedArticleReadLog(userId, articleId, firstReadAt);
		Long secondLogId = summarizedArticleReadLogService.saveSummarizedArticleReadLog(userId, articleId, secondReadAt);

		// then
		assertThat(firstLogId).isNotEqualTo(secondLogId);
		assertThat(summarizedArticleReadLogRepository.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("다른 사용자가 같은 기사를 읽으면 각각 로그가 저장된다")
	void should_saveMultipleLogs_when_differentUsersReadSameArticle() {
		// given
		Long user1Id = 1L;
		Long user2Id = 2L;
		String articleId = "test-article-id";
		LocalDateTime readAt = LocalDateTime.now();

		// when
		Long log1Id = summarizedArticleReadLogService.saveSummarizedArticleReadLog(user1Id, articleId, readAt);
		Long log2Id = summarizedArticleReadLogService.saveSummarizedArticleReadLog(user2Id, articleId, readAt);

		// then
		assertThat(log1Id).isNotEqualTo(log2Id);
		assertThat(summarizedArticleReadLogRepository.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("저장된 로그는 생성 시간과 수정 시간이 자동으로 기록된다")
	void should_haveCreatedAtAndUpdatedAt_when_logSaved() {
		// given
		Long userId = 1L;
		String articleId = "test-article-id";
		LocalDateTime readAt = LocalDateTime.now();

		// when
		Long savedLogId = summarizedArticleReadLogService.saveSummarizedArticleReadLog(userId, articleId, readAt);

		// then
		SummarizedArticleReadLog savedLog = summarizedArticleReadLogRepository.findById(savedLogId)
			.orElseThrow();

		assertThat(savedLog.getCreatedAt()).isNotNull();
		assertThat(savedLog.getUpdatedAt()).isNotNull();
	}
}
