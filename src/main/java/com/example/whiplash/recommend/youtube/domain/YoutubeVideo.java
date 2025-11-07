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

    private String videoId;

    private String videoUrl;

    private String channel;

    private Double recommendationScore;

    private Double qualityScore;

    private Double relevanceScore;

    private Double educationalValue;

    private Double contentAccuracy;

    private String analysisSummary;

    private String trustComment;

    private VideoMetrics metrics;
}
