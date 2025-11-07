package com.example.whiplash.recommend.youtube.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 유튜브 영상 메트릭 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetrics implements Serializable {
    private static final long serialVersionUID = 1L;

    private String viewCount;

    private String likeCount;

    private Integer commentCount;

    private Double positiveRatio;
}
