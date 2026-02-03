package com.example.whiplash.recommend.youtube.repository;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("유튜브 추천 Redis Repository 테스트")
class ArticleRecommendRedisRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private YoutubeRecommendRedisRepository repository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    @MockBean
    private SetOperations<String, Object> setOperations;

    @Test
    @DisplayName("추천 영상 목록을 Redis에 성공적으로 저장해야 한다")
    void should_storeRecommendations_when_validVideosProvided() {
        // given
        Long keywordId = 1L;
        List<YoutubeVideo> videos = createMockVideos(10);
        String expectedKey = "youtube:keyword:1";

        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(redisTemplate.delete(expectedKey)).willReturn(true);
        given(setOperations.add(eq(expectedKey), any(YoutubeVideo.class))).willReturn(1L);

        // when
        repository.storeRecommendations(keywordId, videos);

        // then
        verify(redisTemplate).delete(expectedKey);
        verify(setOperations, times(10)).add(eq(expectedKey), any(YoutubeVideo.class));
    }

    @Test
    @DisplayName("빈 영상 목록은 저장하지 않아야 한다")
    void should_notStore_when_emptyVideoList() {
        // given
        Long keywordId = 1L;
        List<YoutubeVideo> emptyVideos = new ArrayList<>();

        // when
        repository.storeRecommendations(keywordId, emptyVideos);

        // then
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).opsForSet();
    }

    @Test
    @DisplayName("null 영상 목록은 저장하지 않아야 한다")
    void should_notStore_when_nullVideoList() {
        // given
        Long keywordId = 1L;

        // when
        repository.storeRecommendations(keywordId, null);

        // then
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).opsForSet();
    }

    @Test
    @DisplayName("기존 데이터를 삭제한 후 새로운 추천을 저장해야 한다")
    void should_deleteOldDataBeforeStoring_when_newRecommendations() {
        // given
        Long keywordId = 5L;
        List<YoutubeVideo> videos = createMockVideos(3);
        String expectedKey = "youtube:keyword:5";

        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(redisTemplate.delete(expectedKey)).willReturn(true);
        given(setOperations.add(eq(expectedKey), any(YoutubeVideo.class))).willReturn(1L);

        // when
        repository.storeRecommendations(keywordId, videos);

        // then
        // verify delete is called first
        verify(redisTemplate).delete(expectedKey);
        // then verify add is called
        verify(setOperations, times(3)).add(eq(expectedKey), any(YoutubeVideo.class));
    }

    @Test
    @DisplayName("여러 키워드에 대한 추천을 각각 저장할 수 있어야 한다")
    void should_storeSeparately_when_differentKeywords() {
        // given
        Long keywordId1 = 1L;
        Long keywordId2 = 2L;
        List<YoutubeVideo> videos1 = createMockVideos(5);
        List<YoutubeVideo> videos2 = createMockVideos(7);

        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(redisTemplate.delete(anyString())).willReturn(true);
        given(setOperations.add(anyString(), any(YoutubeVideo.class))).willReturn(1L);

        // when
        repository.storeRecommendations(keywordId1, videos1);
        repository.storeRecommendations(keywordId2, videos2);

        // then
        verify(redisTemplate).delete("youtube:keyword:1");
        verify(redisTemplate).delete("youtube:keyword:2");
        verify(setOperations, times(5 + 7)).add(anyString(), any(YoutubeVideo.class));
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
