package com.example.whiplash.recommend.youtube.web.dto.response;

import java.util.Collections;
import java.util.List;

public record YoutubeRecommendResponse(
        Integer totalCount,
        Integer keywordBasedCount,
        Integer commonRecommendCount,
        List<YoutubeVideoDTO> videos
) {
    public static YoutubeRecommendResponse of(
            List<YoutubeVideoDTO> videos,
            int keywordBasedCount,
            int commonRecommendCount
    ) {
        return new YoutubeRecommendResponse(
                videos.size(),
                keywordBasedCount,
                commonRecommendCount,
                videos
        );
    }

    public static YoutubeRecommendResponse empty() {
        return YoutubeRecommendResponse.of(Collections.emptyList(), 0, 0);
    }
}
