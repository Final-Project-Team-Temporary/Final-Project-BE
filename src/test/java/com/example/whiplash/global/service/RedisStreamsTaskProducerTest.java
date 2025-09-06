package com.example.whiplash.global.service;

import com.example.whiplash.RedisTestSupport;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RedisStreamsTaskProducerTest extends RedisTestSupport {
    @Value("${redis.key.stream-key}")
    private String STREAM_KEY;
    @Value("${redis.key.dedup-key}")
    private String DEDUP_KEY;
    @Autowired
    private RedisStreamsTaskProducer redisStreamsProducer;

    @DisplayName("새로운 기사ID를 전달받으면 Stream타입 작업큐에 데이터를 넣는다")
    @Test
    public void should_produce_task_1_when_receive_new_article_id() {
        // when
        LocalDateTime publishedAt = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        redisStreamsProducer.produce("1", publishedAt);

        // then
        Set<Object> setValues = redisTemplate.opsForSet()
                .members(DEDUP_KEY);
        assertThat(setValues).hasSize(1);

        List<MapRecord<String, Object, Object>> streamValues = redisTemplate.opsForStream()
                .range(STREAM_KEY, Range.unbounded());

        assertThat(streamValues).hasSize(1)
                .extracting(MapRecord::getValue)
                .containsExactlyInAnyOrder(
                        Map.ofEntries(
                                MapEntry.entry("articleId", "1"),
                                MapEntry.entry("timestamp", publishedAt.toString())
                        )
                )
                .allSatisfy(value -> {
                    assertThat(value.get("articleId")).isEqualTo("1");
                    assertThat(value.get("timestamp")).isEqualTo(publishedAt.toString());
                })
        ;
    }

    @DisplayName("새로운 기사ID를 전달받으면 Stream타입 작업큐에 데이터를 넣는다")
    @Test
    public void should_produce_task_2_when_receive_new_article_idasdf() {
        // when
        LocalDateTime publishedAt = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        redisStreamsProducer.produce("1", publishedAt);
        redisStreamsProducer.produce("2", publishedAt);

        // then
        Set<Object> setValues = redisTemplate.opsForSet()
                .members(DEDUP_KEY);
        assertThat(setValues).hasSize(2);

        List<MapRecord<String, Object, Object>> streamValues = redisTemplate.opsForStream()
                .range(STREAM_KEY, Range.unbounded());
        assertThat(streamValues).hasSize(2)
                .extracting(MapRecord::getValue)
                .containsExactlyInAnyOrder(
                        Map.ofEntries(
                                MapEntry.entry("articleId", "1"),
                                MapEntry.entry("timestamp", publishedAt.toString())
                        ),
                        Map.ofEntries(
                                MapEntry.entry("articleId", "2"),
                                MapEntry.entry("timestamp", publishedAt.toString())
                        )
                )
        ;
    }
}