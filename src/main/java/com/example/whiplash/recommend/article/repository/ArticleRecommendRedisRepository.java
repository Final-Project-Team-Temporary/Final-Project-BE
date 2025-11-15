package com.example.whiplash.recommend.article.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.user.domain.User;

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

	private final RedisTemplate<String, Object> redisTemplate;

	public ArticleRecommendResult findByUser(User user) {
		String key = buildRedisKey(user.getId());
		return (ArticleRecommendResult)redisTemplate.opsForValue().get(key);
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
