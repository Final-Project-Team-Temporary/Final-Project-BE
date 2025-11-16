package com.example.whiplash.recommend.article.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유튜브 추천 영상을 Redis에서 조회/저장하는 Repository
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ArticleRecommendRedisRepository {
	private static final String ARTICLE_RECOMMEND_KEY = "article:recommend:user:%d";
	private final ObjectMapper objectMapper;

	private final RedisTemplate<String, Object> redisTemplate;

	public ArticleRecommendResult findByUser(User user) {
		String key = buildRedisKey(user.getId());

		Object resultObj = redisTemplate.opsForValue().get(key);
		try {
			if (resultObj == null) {
				return null;
			}

			// 이미 ArticleRecommendResult 타입이면 바로 반환
			if (resultObj instanceof ArticleRecommendResult) {
				return (ArticleRecommendResult) resultObj;
			}

			// String (JSON)이면 바로 파싱
			if (resultObj instanceof String) {
				return objectMapper.readValue((String) resultObj, ArticleRecommendResult.class);
			}

			// Map/LinkedHashMap이면 convertValue 사용
			if (resultObj instanceof Map) {
				return objectMapper.convertValue(resultObj, ArticleRecommendResult.class);
			}

			// 그 외의 경우 (예외 케이스)
			String rawJson = objectMapper.writeValueAsString(resultObj);
			return objectMapper.readValue(rawJson, ArticleRecommendResult.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

	}

	public void storeRecommendations(ArticleRecommendResult recommendResult) {
		String key = buildRedisKey(recommendResult.getUser().getId());
		redisTemplate.delete(key);

		redisTemplate.opsForValue().set(key, recommendResult);
	}

	private static String buildRedisKey(Long userId) {
		String key = String.format(ARTICLE_RECOMMEND_KEY, userId);
		return key;
	}
}
