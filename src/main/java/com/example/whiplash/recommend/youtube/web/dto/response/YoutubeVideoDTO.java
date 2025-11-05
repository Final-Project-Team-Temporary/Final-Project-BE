package com.example.whiplash.recommend.youtube.web.dto.response;

import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.fasterxml.jackson.annotation.JsonProperty;

public record YoutubeVideoDTO(
        Integer rank,
        String title,
        @JsonProperty("video_id")
        String videoId,
        @JsonProperty("video_url")
        String videoUrl,
        String channel,
        @JsonProperty("recommendation_score")
        Double recommendationScore,
        @JsonProperty("quality_score")
        Double qualityScore,
        @JsonProperty("relevance_score")
        Double relevanceScore,
        @JsonProperty("educational_value")
        Double educationalValue,
        @JsonProperty("content_accuracy")
        Double contentAccuracy,
        @JsonProperty("analysis_summary")
        String analysisSummary,
        @JsonProperty("trust_comment")
        String trustComment,
        VideoMetricsDTO metrics
) {
    public static YoutubeVideoDTO from(YoutubeVideo video) {
        return new YoutubeVideoDTO(
            video.getRank(),
            video.getTitle(),
            video.getVideoId(),
            video.getVideoUrl(),
            video.getChannel(),
            video.getRecommendationScore(),
            video.getQualityScore(),
            video.getRelevanceScore(),
            video.getEducationalValue(),
            video.getContentAccuracy(),
            video.getAnalysisSummary(),
            video.getTrustComment(),
            VideoMetricsDTO.from(video.getMetrics())
        );
    }
}

