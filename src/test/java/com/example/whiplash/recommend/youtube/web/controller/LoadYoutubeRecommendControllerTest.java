package com.example.whiplash.recommend.youtube.web.controller;

import com.example.whiplash.MvcTestSupport;
import com.example.whiplash.recommend.youtube.service.LoadYoutubeRecommendService;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeRecommendResponse;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeVideoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("유튜브 추천 컨트롤러 테스트")
@AutoConfigureMockMvc(addFilters = false)
class LoadYoutubeRecommendControllerTest extends MvcTestSupport {

    @MockitoBean
    private LoadYoutubeRecommendService loadYoutubeRecommendService;

    @Test
    @DisplayName("유튜브 추천 API가 성공적으로 추천 목록을 반환해야 한다")
    void should_returnRecommendations_when_validRequest() throws Exception {
        // given
        List<YoutubeVideoDTO> videos = createMockVideoDTOs(10);
        YoutubeRecommendResponse response = YoutubeRecommendResponse.of(videos, 7, 3);

        given(loadYoutubeRecommendService.getRecommendations(any(Optional.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/recommends/videos/youtube"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.keywordBasedCount").value(7))
                .andExpect(jsonPath("$.data.commonRecommendCount").value(3))
                .andExpect(jsonPath("$.data.videos").isArray())
                .andExpect(jsonPath("$.data.videos.length()").value(10))
                .andExpect(jsonPath("$.data.videos[0].rank").value(1))
                .andExpect(jsonPath("$.data.videos[0].video_id").value("video-0"))
                .andExpect(jsonPath("$.data.videos[0].title").value("Video Title 0"))
                .andExpect(jsonPath("$.data.videos[0].channel").value("Channel 0"));
    }

    @Test
    @DisplayName("유튜브 추천 API가 키워드 기반 추천만 반환할 수 있어야 한다")
    void should_returnKeywordBasedRecommendationsOnly_when_sufficient() throws Exception {
        // given
        List<YoutubeVideoDTO> videos = createMockVideoDTOs(15);
        YoutubeRecommendResponse response = YoutubeRecommendResponse.of(videos, 15, 0);

        given(loadYoutubeRecommendService.getRecommendations(any(Optional.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/recommends/videos/youtube"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(15))
                .andExpect(jsonPath("$.data.keywordBasedCount").value(15))
                .andExpect(jsonPath("$.data.commonRecommendCount").value(0));
    }

    @Test
    @DisplayName("유튜브 추천 API가 공통 추천만 반환할 수 있어야 한다")
    void should_returnCommonRecommendationsOnly_when_noKeywords() throws Exception {
        // given
        List<YoutubeVideoDTO> videos = createMockVideoDTOs(10);
        YoutubeRecommendResponse response = YoutubeRecommendResponse.of(videos, 0, 10);

        given(loadYoutubeRecommendService.getRecommendations(any(Optional.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/recommends/videos/youtube"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.keywordBasedCount").value(0))
                .andExpect(jsonPath("$.data.commonRecommendCount").value(10));
    }

    // Helper methods
    private List<YoutubeVideoDTO> createMockVideoDTOs(int count) {
        List<YoutubeVideoDTO> videos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            videos.add(new YoutubeVideoDTO(
                    i + 1,                                              // rank
                    "Video Title " + i,                                 // title
                    "video-" + i,                                       // videoId
                    "https://youtube.com/watch?v=video-" + i,          // videoUrl
                    "Channel " + i,                                     // channel
                    85.5 + i,                                           // recommendationScore
                    78.2 + i,                                           // qualityScore
                    95.0 + i,                                           // relevanceScore
                    88.5 + i,                                           // educationalValue
                    92.3 + i,                                           // contentAccuracy
                    "Analysis summary " + i,                            // analysisSummary
                    "Trust comment " + i,                               // trustComment
                    new com.example.whiplash.recommend.youtube.web.dto.response.VideoMetricsDTO(
                            String.valueOf(i * 1000),                   // viewCount
                            String.valueOf(i * 100),                    // likeCount
                            i * 10,                                     // commentCount
                            85.2 + i                                    // positiveRatio
                    )
            ));
        }
        return videos;
    }
}
