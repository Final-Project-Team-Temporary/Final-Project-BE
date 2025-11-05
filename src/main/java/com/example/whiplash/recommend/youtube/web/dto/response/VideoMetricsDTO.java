package com.example.whiplash.recommend.youtube.web.dto.response;

import com.example.whiplash.recommend.youtube.domain.VideoMetrics;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VideoMetricsDTO(
        @JsonProperty("view_count")
        String viewCount,

        @JsonProperty("like_count")
        String likeCount,

        @JsonProperty("comment_count")
        Integer commentCount,

        @JsonProperty("positive_ratio")
        Double positiveRatio
) {
    public static VideoMetricsDTO from(VideoMetrics metrics) {
        if (metrics == null) {
            return null;
        }
        return new VideoMetricsDTO(
                metrics.getViewCount(),
                metrics.getLikeCount(),
                metrics.getCommentCount(),
                metrics.getPositiveRatio()
        );
    }
}
