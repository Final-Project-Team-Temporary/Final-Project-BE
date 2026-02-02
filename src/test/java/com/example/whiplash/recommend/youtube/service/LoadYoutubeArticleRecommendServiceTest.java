package com.example.whiplash.recommend.youtube.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.recommend.youtube.repository.YoutubeRecommendRedisRepository;
import com.example.whiplash.recommend.youtube.streams.producer.YoutubeRecommendTaskProducer;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeRecommendResponse;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.user.repository.keyword.KeywordRepository;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("유튜브 추천 서비스 테스트")
class LoadYoutubeArticleRecommendServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private UserKeywordRepository userKeywordRepository;

    @MockBean
    private YoutubeRecommendRedisRepository youtubeRecommendRedisRepository;

    @MockBean
    private YoutubeRecommendTaskProducer youtubeRecommendTaskProducer;

    @Autowired
    private LoadYoutubeRecommendService loadYoutubeRecommendService;

    @Test
    @DisplayName("인증되지 않은 사용자가 추천을 요청하면 예외를 던져야 한다")
    void should_throwUnauthorizedException_when_userNotAuthenticated() {
        // given
        Optional<String> emptyEmail = Optional.empty();

        // when & then
        assertThatThrownBy(() -> loadYoutubeRecommendService.getRecommendations(emptyEmail))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.UNAUTHORIZED);
                });
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 추천을 요청하면 예외를 던져야 한다")
    void should_throwUserNotFoundException_when_userNotExists() {
        // given
        String nonExistentEmail = "nonexistent@example.com";

        // when & then
        assertThatThrownBy(() -> loadYoutubeRecommendService.getRecommendations(Optional.of(nonExistentEmail)))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("키워드가 없는 사용자는 공통 추천만 받아야 한다")
    void should_returnCommonRecommendationsOnly_when_userHasNoKeywords() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        List<YoutubeVideo> commonVideos = createMockVideos(10);
        given(youtubeRecommendRedisRepository.findCommonRecommendations(10))
                .willReturn(commonVideos);

        // when
        YoutubeRecommendResponse response = loadYoutubeRecommendService
                .getRecommendations(Optional.of(user.getEmail()));

        // then
        assertThat(response)
                .satisfies(res -> {
                    assertThat(res.totalCount()).isEqualTo(10);
                    assertThat(res.keywordBasedCount()).isEqualTo(0);
                    assertThat(res.commonRecommendCount()).isEqualTo(10);
                    assertThat(res.videos()).hasSize(10);
                });
    }

    @Test
    @DisplayName("키워드 기반 추천이 10개 이상이면 공통 추천을 포함하지 않아야 한다")
    void should_notIncludeCommonRecommendations_when_keywordBasedVideosAreSufficient() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        Keyword keyword = createKeyword("Java");
        keywordRepository.save(keyword);

        UserKeyword userKeyword = UserKeyword.create(user, keyword);
        userKeywordRepository.save(userKeyword);

        List<YoutubeVideo> keywordVideos = createMockVideos(15);
        given(youtubeRecommendRedisRepository.findByKeywordId(keyword.getId()))
                .willReturn(keywordVideos);

        // when
        YoutubeRecommendResponse response = loadYoutubeRecommendService
                .getRecommendations(Optional.of(user.getEmail()));

        // then
        assertThat(response)
                .satisfies(res -> {
                    assertThat(res.totalCount()).isEqualTo(15);
                    assertThat(res.keywordBasedCount()).isEqualTo(15);
                    assertThat(res.commonRecommendCount()).isEqualTo(0);
                });

        verify(youtubeRecommendRedisRepository, never()).findCommonRecommendations(anyInt());
    }

    @Test
    @DisplayName("키워드 기반 추천이 10개 미만이면 공통 추천으로 채워야 한다")
    void should_fillWithCommonRecommendations_when_keywordBasedVideosAreInsufficient() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        Keyword keyword = createKeyword("Spring");
        keywordRepository.save(keyword);

        UserKeyword userKeyword = UserKeyword.create(user, keyword);
        userKeywordRepository.save(userKeyword);

        List<YoutubeVideo> keywordVideos = createMockVideos(5);
        given(youtubeRecommendRedisRepository.findByKeywordId(keyword.getId()))
                .willReturn(keywordVideos);

        List<YoutubeVideo> commonVideos = createMockVideos(5, 100);
        given(youtubeRecommendRedisRepository.findCommonRecommendations(5))
                .willReturn(commonVideos);

        // when
        YoutubeRecommendResponse response = loadYoutubeRecommendService
                .getRecommendations(Optional.of(user.getEmail()));

        // then
        assertThat(response)
                .satisfies(res -> {
                    assertThat(res.totalCount()).isEqualTo(10);
                    assertThat(res.keywordBasedCount()).isEqualTo(5);
                    assertThat(res.commonRecommendCount()).isEqualTo(5);
                });

        verify(youtubeRecommendRedisRepository).findCommonRecommendations(5);
    }

    @Test
    @DisplayName("여러 키워드의 추천 영상을 중복 없이 합쳐야 한다")
    void should_mergeVideosFromMultipleKeywordsWithoutDuplicates() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        Keyword keyword1 = createKeyword("Java");
        Keyword keyword2 = createKeyword("Spring");
        keywordRepository.save(keyword1);
        keywordRepository.save(keyword2);

        UserKeyword userKeyword1 = UserKeyword.create(user, keyword1);
        UserKeyword userKeyword2 = UserKeyword.create(user, keyword2);
        userKeywordRepository.save(userKeyword1);
        userKeywordRepository.save(userKeyword2);

        List<YoutubeVideo> videos1 = createMockVideos(7);
        List<YoutubeVideo> videos2 = createMockVideos(5, 7);
        given(youtubeRecommendRedisRepository.findByKeywordId(keyword1.getId()))
                .willReturn(videos1);
        given(youtubeRecommendRedisRepository.findByKeywordId(keyword2.getId()))
                .willReturn(videos2);

        // when
        YoutubeRecommendResponse response = loadYoutubeRecommendService
                .getRecommendations(Optional.of(user.getEmail()));

        // then
        assertThat(response)
                .satisfies(res -> {
                    assertThat(res.totalCount()).isEqualTo(12);
                    assertThat(res.keywordBasedCount()).isEqualTo(12);
                    assertThat(res.commonRecommendCount()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("키워드 ID와 이름이 Redis Streams에 비동기로 발행되어야 한다")
    void should_publishKeywordIdAndNameToRedisStreams_when_noRecommendationsFound() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        Keyword keyword = createKeyword("Docker");
        keywordRepository.save(keyword);

        UserKeyword userKeyword = UserKeyword.create(user, keyword);
        userKeywordRepository.save(userKeyword);

        given(youtubeRecommendRedisRepository.findByKeywordId(keyword.getId()))
                .willReturn(new ArrayList<>());

        given(youtubeRecommendRedisRepository.findCommonRecommendations(10))
                .willReturn(createMockVideos(10));

        // when
        loadYoutubeRecommendService.getRecommendations(Optional.of(user.getEmail()));

        // then
        verify(youtubeRecommendTaskProducer).produceKeywordRecommendTask(keyword.getId(), keyword.getName());
    }

    // Helper methods
    private User createUser(String email) {
        return User.builder()
                .email(email)
                .name("Test User")
                .build();
    }

    private Keyword createKeyword(String name) {
        return Keyword.create(name);
    }

    private List<YoutubeVideo> createMockVideos(int count) {
        return createMockVideos(count, 0);
    }

    private List<YoutubeVideo> createMockVideos(int count, int startIndex) {
        List<YoutubeVideo> videos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = startIndex + i;
            videos.add(YoutubeVideo.builder()
                    .rank(index + 1)
                    .title("Video Title " + index)
                    .videoId("video-" + index)
                    .videoUrl("https://youtube.com/watch?v=video-" + index)
                    .channel("Channel " + index)
                    .recommendationScore(85.5 + index)
                    .qualityScore(78.2 + index)
                    .relevanceScore(95.0 + index)
                    .educationalValue(88.5 + index)
                    .contentAccuracy(92.3 + index)
                    .analysisSummary("Analysis summary " + index)
                    .trustComment("Trust comment " + index)
                    .metrics(com.example.whiplash.recommend.youtube.domain.VideoMetrics.builder()
                            .viewCount(String.valueOf(index * 1000))
                            .likeCount(String.valueOf(index * 100))
                            .commentCount(index * 10)
                            .positiveRatio(85.2 + index)
                            .build())
                    .build());
        }
        return videos;
    }
}
