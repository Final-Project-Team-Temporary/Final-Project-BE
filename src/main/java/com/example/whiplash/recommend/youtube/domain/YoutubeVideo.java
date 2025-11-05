package com.example.whiplash.recommend.youtube.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 유튜브 영상 정보를 담는 도메인 객체
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeVideo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer rank;

    private String title;

    @JsonProperty("video_id")
    private String videoId;

    @JsonProperty("video_url")
    private String videoUrl;

    private String channel;

    @JsonProperty("recommendation_score")
    private Double recommendationScore;

    @JsonProperty("quality_score")
    private Double qualityScore;

    @JsonProperty("relevance_score")
    private Double relevanceScore;

    @JsonProperty("educational_value")
    private Double educationalValue;

    @JsonProperty("content_accuracy")
    private Double contentAccuracy;

    @JsonProperty("analysis_summary")
    private String analysisSummary;

    @JsonProperty("trust_comment")
    private String trustComment;

    private VideoMetrics metrics;
}
