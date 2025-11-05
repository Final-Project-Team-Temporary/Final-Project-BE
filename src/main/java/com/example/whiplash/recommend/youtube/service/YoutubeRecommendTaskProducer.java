package com.example.whiplash.recommend.youtube.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 유튜브 추천 작업을 Redis Streams에 발행하는 Producer
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class YoutubeRecommendTaskProducer {
    @Value("${redis.key.recommend.stream-key}")
    private String STREAM_KEY;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 키워드 ID를 Redis Streams에 비동기로 발행
     *
     * @param keywordId 키워드 ID
     * @param keywordName 키워드 이름
     */
    @Async
    public void produceKeywordRecommendTask(Long keywordId, String keywordName) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("keywordId", keywordId.toString());
            body.put("keywordName", keywordName);
            body.put("timestamp", LocalDateTime.now().toString());

            RecordId recordId = redisTemplate.opsForStream()
                    .add(ObjectRecord.create(STREAM_KEY, body));

            log.info("Published keywordId={}, keywordName='{}' to stream with recordId={}, stream-key: {}",
                    keywordId, keywordName, recordId, STREAM_KEY);
        } catch (Exception e) {
            log.error("Failed to publish keywordId={} to Redis Streams", keywordId, e);
        }
    }
}
