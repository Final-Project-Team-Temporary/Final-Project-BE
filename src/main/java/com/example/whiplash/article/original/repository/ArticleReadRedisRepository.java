package com.example.whiplash.article.original.repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ArticleReadRedisRepository {
	private static final String KEY = "article:read:userId:%d:date:%s";

	private final RedisTemplate<String, Object> redisTemplate;

	public boolean isRead(Long userId, String articleId) {
		Boolean isRead = redisTemplate.opsForSet().isMember(getKey(userId), articleId);
		return isRead != null && isRead;
	}

	public void readArticle(Long userId, String articleId) {
		String key = getKey(userId);

		redisTemplate.opsForSet().add(key, articleId);

		redisTemplate.expire(key, Duration.ofDays(1));
	}

	private String getKey(Long userId) {
		String today = LocalDate.now()
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		return String.format(KEY, userId, today);
	}
}
