package com.example.whiplash.recommend.youtube.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.recommend.youtube.service.LoadYoutubeRecommendService;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeRecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유튜브 영상 추천 API Controller
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/recommends/videos/youtube")
@RestController
public class LoadYoutubeRecommendController {

    private final LoadYoutubeRecommendService loadYoutubeRecommendService;

    /**
     * 키워드 기반 유튜브 영상 추천 조회
     * <p>
     * 인증된 사용자의 키워드를 기반으로 추천 영상을 조회합니다.
     * 키워드 기반 추천이 10개 미만인 경우 공통 추천으로 부족한 개수를 채웁니다.
     * 동시에 비동기로 Redis Streams에 키워드 ID를 발행합니다.
     *
     * @return 추천 영상 목록 (최소 10개)
     */
    @GetMapping
    public ApiResponse<YoutubeRecommendResponse> getRecommendations() {
        log.info("유튜브 영상 추천 요청 수신");

        YoutubeRecommendResponse response = loadYoutubeRecommendService.getKeywordBasedRecommendations(
                SecurityContextUtils.getCurrentUserId()
        );

        log.info("유튜브 영상 추천 완료: totalCount={}, keywordBasedCount={}, commonCount={}",
                response.totalCount(),
                response.keywordBasedCount(),
                response.commonRecommendCount());

        return ApiResponse.onSuccess(response);
    }
}
