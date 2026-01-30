package com.example.whiplash.global.service;

import com.example.whiplash.article.summary.service.ArticleTaskProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RedisStreamsTaskProducer implements ArticleTaskProducer {

	private final String STREAM_KEY;
	private final String DEDUP_KEY;
	private final RedisTemplate<String, Object> redisTemplate;

	public RedisStreamsTaskProducer(@Value("${redis.key.stream-key}") String STREAM_KEY,
									@Value("${redis.key.dedup-key}") String DEDUP_KEY,
									RedisTemplate<String, Object> redisTemplate) {
		this.STREAM_KEY = STREAM_KEY;
		this.DEDUP_KEY = DEDUP_KEY;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public String produce(String articleId, LocalDateTime timestamp) {
		Map<String, String> body = new HashMap<>();
		body.put("articleId", articleId);
		body.put("timestamp", timestamp.toString());

		boolean isNewArticle = redisTemplate.opsForSet()
			.add(DEDUP_KEY, articleId) > 0; // 새 값이면 1, 중복이면 0
		if (!isNewArticle) {
			log.info("ArticleId={} is already produced", articleId);
			return null;
		}

		RecordId recordId = redisTemplate.opsForStream()
			.add(ObjectRecord.create(STREAM_KEY, body));
		log.info("Published articleId={} stream with recordId={} stream-key: {}", articleId, recordId, STREAM_KEY);

		return recordId.getValue();
	}
}
