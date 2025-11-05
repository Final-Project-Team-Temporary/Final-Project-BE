package com.example.whiplash.recommend.youtube.repository;

import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 유튜브 추천 영상을 Redis에서 조회/저장하는 Repository
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class YoutubeRecommendRedisRepository {
    @Value("${redis.key.recommend.keyword-recommend}")
    private String KEYWORD_RECOMMEND_PREFIX;
    @Value("${redis.key.recommend.common-recommend}")
    private String COMMON_RECOMMEND_KEY;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 키워드 ID 기반으로 추천 영상 목록 조회
     *
     * @param keywordId 키워드 ID
     * @return 추천 영상 목록
     */
    public List<YoutubeVideo> findByKeywordId(Long keywordId) {
        String key = KEYWORD_RECOMMEND_PREFIX + keywordId;
        return getVideosFromRedis(key);
    }

    /**
     * 공통 추천 영상 목록 조회
     *
     * @param limit 조회할 개수
     * @return 공통 추천 영상 목록
     */
    public Set<YoutubeVideo> findCommonRecommendations(int limit) {
        List<YoutubeVideo> allVideos = getVideosFromRedis(COMMON_RECOMMEND_KEY);

        // limit 만큼만 반환
        if (allVideos.size() <= limit) {
            return new HashSet<>(allVideos);
        }

        return new HashSet<>(allVideos.subList(0, limit));
    }

    /**
     * Redis Set에서 YoutubeVideo 목록 조회
     *
     * @param key Redis key
     * @return 영상 목록
     */
    private List<YoutubeVideo> getVideosFromRedis(String key) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);

            if (members == null || members.isEmpty()) {
                log.debug("No videos found for key: {}", key);
                return new ArrayList<>();
            }

            return members.stream()
                    .filter(obj -> obj instanceof YoutubeVideo)
                    .map(obj -> (YoutubeVideo) obj)
                    .toList();
        } catch (Exception e) {
            log.error("Error while fetching videos from Redis for key: {}", key, e);
            return new ArrayList<>();
        }
    }

    /**
     * 키워드 ID에 대한 추천 영상 목록을 Redis에 저장
     *
     * @param keywordId 키워드 ID
     * @param videos    추천 영상 목록
     */
    public void storeRecommendations(Long keywordId, List<YoutubeVideo> videos) {
        if (videos == null || videos.isEmpty()) {
            log.warn("No videos to store for keywordId={}", keywordId);
            return;
        }

        String key = KEYWORD_RECOMMEND_PREFIX + keywordId;

        try {
            // 기존 데이터 삭제
            redisTemplate.delete(key);

            // 새로운 추천 영상 저장 (Set으로 저장)
            for (YoutubeVideo video : videos) {
                redisTemplate.opsForSet().add(key, video);
            }

            log.info("Successfully stored {} videos for keywordId={} in Redis key={}",
                videos.size(), keywordId, key);
        } catch (Exception e) {
            log.error("Failed to store recommendations for keywordId={}", keywordId, e);
            throw e;
        }
    }
}
