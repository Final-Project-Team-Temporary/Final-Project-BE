package com.example.whiplash.recommend.article.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.whiplash.recommend.article.application.RecommendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기사 추천 스케줄러
 * 매일 12시(정오)에 모든 사용자에 대한 기사 추천을 수행합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleRecommendScheduler {
	private final RecommendService recommendService;

	/**
	 * 매일 12시에 기사 추천 실행
	 * cron = "초 분 시 일 월 요일"
	 * 0 0 12 * * * = 매일 12시 0분 0초
	 */
	@Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
	public void scheduleArticleRecommendations() {
		log.info("기사 추천 스케줄 시작 - {}", LocalDateTime.now());

		try {
			LocalDateTime now = LocalDateTime.now();
			// 최근 24시간 이내 발행된 기사를 기준으로 추천
			LocalDateTime publishedAtAfter = now.minusDays(1);

			recommendService.recommendArticle(now, publishedAtAfter);

			log.info("기사 추천 스케줄 완료 - {}", LocalDateTime.now());
		} catch (Exception e) {
			log.error("기사 추천 스케줄 실행 중 오류 발생", e);
		}
	}
}
