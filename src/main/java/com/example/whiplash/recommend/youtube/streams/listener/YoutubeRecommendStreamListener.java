package com.example.whiplash.recommend.youtube.streams.listener;

import com.example.whiplash.recommend.youtube.service.YoutubeRecommendStorageService;
import com.example.whiplash.recommend.youtube.streams.dto.YoutubeRecommendStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis Streams에서 유튜브 추천 작업 결과를 수신하는 Listener
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class YoutubeRecommendStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final YoutubeRecommendStorageService storageService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            log.info("Received stream message: id={}, stream={}", record.getId(), record.getStream());

            // 메시지 데이터 추출
            Map<String, String> messageData = record.getValue();

            // JSON 문자열로 변환 후 DTO로 파싱
            // AI 서버가 보낼 메시지 형식: {"keywordId": "123", "videos": "[...]"}
            String keywordId = messageData.get("keywordId");
            String videosJson = messageData.get("videos");

            if (keywordId == null || videosJson == null) {
                log.error("Invalid message format: missing keywordId or videos. message={}", messageData);
                return;
            }

            // DTO로 변환
            YoutubeRecommendStreamMessage message = new YoutubeRecommendStreamMessage(
                    keywordId,
                    objectMapper.readValue(
                            videosJson,
                            objectMapper.getTypeFactory().constructCollectionType(
                                    java.util.List.class,
                                    com.example.whiplash.recommend.youtube.domain.YoutubeVideo.class
                            )
                    )
            );

            log.info("Processing recommendation for keywordId={}, videoCount={}",
                    message.keywordId(), message.videos().size());

            // Redis에 추천 영상 저장
            storageService.storeRecommendations(message.getKeywordIdAsLong(), message.videos());

            log.info("Successfully processed recommendation for keywordId={}", message.keywordId());

        } catch (Exception e) {
            log.error("Failed to process stream message: id={}, error={}",
                    record.getId(), e.getMessage(), e);
            // 여기서 DLQ(Dead Letter Queue)로 보내거나 재시도 로직 추가 가능
        }
    }
}
