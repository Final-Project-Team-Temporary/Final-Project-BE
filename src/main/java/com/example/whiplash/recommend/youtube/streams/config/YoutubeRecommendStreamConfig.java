package com.example.whiplash.recommend.youtube.streams.config;

import com.example.whiplash.recommend.youtube.streams.listener.YoutubeRecommendStreamListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

/**
 * Redis Streams Consumer 설정
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class YoutubeRecommendStreamConfig {

    private static final String STREAM_KEY = "youtube-recommend-result-stream";
    private static final String CONSUMER_GROUP = "youtube-recommend-consumer-group";
    private static final String CONSUMER_NAME = "youtube-recommend-consumer";

    private final RedisConnectionFactory redisConnectionFactory;
    private final YoutubeRecommendStreamListener streamListener;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> youtubeRecommendStreamContainer() {
        // Container 옵션 설정
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(2))  // 폴링 타임아웃
                        .build();

        // Container 생성
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // Consumer Group과 Stream 생성 (없으면 자동 생성)
        try {
            redisConnectionFactory.getConnection()
                    .streamCommands()
                    .xGroupCreate(
                            STREAM_KEY.getBytes(),
                            CONSUMER_GROUP,
                            ReadOffset.from("0-0"),
                            true  // mkStream = true (스트림이 없으면 생성)
                    );
            log.info("Created consumer group: {} for stream: {}", CONSUMER_GROUP, STREAM_KEY);
        } catch (Exception e) {
            log.info("Consumer group already exists or creation skipped: {}", e.getMessage());
        }

        // Subscription 등록
        Subscription subscription = container.receive(
                Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                streamListener
        );

        log.info("Registered stream listener for stream: {}, consumer: {}/{}",
                STREAM_KEY, CONSUMER_GROUP, CONSUMER_NAME);

        // Container 시작
        container.start();

        return container;
    }
}

