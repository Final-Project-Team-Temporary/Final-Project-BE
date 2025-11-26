package com.example.whiplash.article.original.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ArticleReadRedisRepository {
	private static final String KEY = "article:read:userId:%d:date:%s";

	private final StringRedisTemplate redisTemplate;

	public boolean isRead(Long userId, String articleId) {
		Double score = redisTemplate.opsForZSet().score(getKey(userId), articleId);
		return score != null;
	}

	public void readArticle(Long userId, String articleId) {
		String key = getKey(userId);

		// 현재 시간을 timestamp로 사용 (score)
		double score = (double)System.currentTimeMillis();
		redisTemplate.opsForZSet().add(key, articleId, score);

		redisTemplate.expire(key, Duration.ofDays(1));
	}

	public Set<String> getArticleIdsReadByUserOnDate(Long userId, LocalDate date, int limit) {
		String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String key = String.format(KEY, userId, dateStr);

		// ZSET에서 score 내림차순으로 최대 limit개 조회 (최신 순)
		// reverseRange: 높은 score(최근 시간)부터 반환
		Set<String> articleIds = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);

		return articleIds != null ? articleIds : Set.of();
	}

	private String getKey(Long userId) {
		String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		return String.format(KEY, userId, today);
	}
}
