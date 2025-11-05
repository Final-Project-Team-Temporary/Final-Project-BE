package com.example.whiplash.recommend.youtube.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String STREAM_KEY = "youtube-recommend-stream";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 키워드 ID를 Redis Streams에 비동기로 발행
     *
     * @param keywordId 키워드 ID
     */
    @Async
    public void produceKeywordRecommendTask(Long keywordId) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("keywordId", keywordId.toString());
            body.put("timestamp", LocalDateTime.now().toString());

            RecordId recordId = redisTemplate.opsForStream()
                    .add(ObjectRecord.create(STREAM_KEY, body));

            log.info("Published keywordId={} to stream with recordId={}, stream-key: {}",
                    keywordId, recordId, STREAM_KEY);
        } catch (Exception e) {
            log.error("Failed to publish keywordId={} to Redis Streams", keywordId, e);
        }
    }
}
