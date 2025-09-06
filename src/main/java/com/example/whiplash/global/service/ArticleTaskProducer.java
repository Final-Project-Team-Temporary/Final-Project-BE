package com.example.whiplash.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleTaskProducer {
    private final String STREAM_KEY = "article-stream";
    private final RedisTemplate<String, Object> redisTemplate;

    public void publishArticle(String articleId, LocalDateTime timestamp) {
        Map<String, String> body = new HashMap<>();
        body.put("articleId", articleId);
        body.put("timestamp", timestamp.toString());

        RecordId recordId = redisTemplate.opsForStream()
                .add(ObjectRecord.create(STREAM_KEY, body));
        log.info("Published articleId={} stream with recordId={}", articleId, recordId);
    }
}
