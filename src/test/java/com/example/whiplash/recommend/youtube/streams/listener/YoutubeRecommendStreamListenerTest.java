package com.example.whiplash.recommend.youtube.streams.listener;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.recommend.youtube.repository.YoutubeRecommendRedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("유튜브 추천 스트림 리스너 테스트")
class YoutubeRecommendStreamListenerTest extends IntegrationTestSupport {

    @Autowired
    private YoutubeRecommendStreamListener streamListener;

    @MockBean
    private YoutubeRecommendRedisRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유효한 스트림 메시지를 수신하면 Redis에 저장해야 한다")
    void should_storeToRedis_when_validMessageReceived() throws JsonProcessingException {
        // given
        Long keywordId = 1L;
        List<YoutubeVideo> videos = createMockVideos(5);

        Map<String, String> messageData = new HashMap<>();
        messageData.put("keywordId", keywordId.toString());
        messageData.put("videos", objectMapper.writeValueAsString(videos));

        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(messageData)
                .withId(RecordId.of("1234567890-0"))
                .withStreamKey("youtube-recommend-result-stream");

        // when
        streamListener.onMessage(record);

        // then
        verify(repository).storeRecommendations(eq(keywordId), anyList());
    }

    @Test
    @DisplayName("keywordId가 없는 메시지는 처리하지 않아야 한다")
    void should_notProcess_when_keywordIdMissing() throws JsonProcessingException {
        // given
        List<YoutubeVideo> videos = createMockVideos(5);

        Map<String, String> messageData = new HashMap<>();
        messageData.put("videos", objectMapper.writeValueAsString(videos));
        // keywordId 누락

        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(messageData)
                .withId(RecordId.of("1234567890-0"))
                .withStreamKey("youtube-recommend-result-stream");

        // when
        streamListener.onMessage(record);

        // then
        verify(repository, never()).storeRecommendations(eq(1L), anyList());
    }

    @Test
    @DisplayName("videos가 없는 메시지는 처리하지 않아야 한다")
    void should_notProcess_when_videosMissing() {
        // given
        Long keywordId = 1L;

        Map<String, String> messageData = new HashMap<>();
        messageData.put("keywordId", keywordId.toString());
        // videos 누락

        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(messageData)
                .withId(RecordId.of("1234567890-0"))
                .withStreamKey("youtube-recommend-result-stream");

        // when
        streamListener.onMessage(record);

        // then
        verify(repository, never()).storeRecommendations(eq(keywordId), anyList());
    }

    @Test
    @DisplayName("잘못된 JSON 형식의 videos는 에러 로그를 남기고 처리를 중단해야 한다")
    void should_logError_when_invalidJsonFormat() {
        // given
        Long keywordId = 1L;

        Map<String, String> messageData = new HashMap<>();
        messageData.put("keywordId", keywordId.toString());
        messageData.put("videos", "invalid-json");

        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(messageData)
                .withId(RecordId.of("1234567890-0"))
                .withStreamKey("youtube-recommend-result-stream");

        // when
        streamListener.onMessage(record);

        // then
        verify(repository, never()).storeRecommendations(eq(keywordId), anyList());
    }

    @Test
    @DisplayName("여러 개의 영상이 포함된 메시지를 정상적으로 처리해야 한다")
    void should_processMultipleVideos_when_validMessageReceived() throws JsonProcessingException {
        // given
        Long keywordId = 5L;
        List<YoutubeVideo> videos = createMockVideos(20);

        Map<String, String> messageData = new HashMap<>();
        messageData.put("keywordId", keywordId.toString());
        messageData.put("videos", objectMapper.writeValueAsString(videos));

        MapRecord<String, String, String> record = StreamRecords
                .mapBacked(messageData)
                .withId(RecordId.of("1234567890-0"))
                .withStreamKey("youtube-recommend-result-stream");

        // when
        streamListener.onMessage(record);

        // then
        verify(repository).storeRecommendations(eq(keywordId), anyList());
    }

    // Helper methods
    private List<YoutubeVideo> createMockVideos(int count) {
        List<YoutubeVideo> videos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            videos.add(YoutubeVideo.builder()
                    .rank(i + 1)
                    .title("Video Title " + i)
                    .videoId("video-" + i)
                    .videoUrl("https://youtube.com/watch?v=video-" + i)
                    .channel("Channel " + i)
                    .recommendationScore(85.5 + i)
                    .qualityScore(78.2 + i)
                    .relevanceScore(95.0 + i)
                    .educationalValue(88.5 + i)
                    .contentAccuracy(92.3 + i)
                    .analysisSummary("Analysis summary " + i)
                    .trustComment("Trust comment " + i)
                    .metrics(com.example.whiplash.recommend.youtube.domain.VideoMetrics.builder()
                            .viewCount(String.valueOf(i * 1000))
                            .likeCount(String.valueOf(i * 100))
                            .commentCount(i * 10)
                            .positiveRatio(85.2 + i)
                            .build())
                    .build());
        }
        return videos;
    }
}
