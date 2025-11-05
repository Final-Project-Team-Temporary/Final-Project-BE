package com.example.whiplash.recommend.youtube.service;

import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 유튜브 추천 영상을 Redis에 저장하는 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class YoutubeRecommendStorageService {
    private static final String KEYWORD_RECOMMEND_PREFIX = "youtube:keyword:";

    private final RedisTemplate<String, Object> redisTemplate;

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
