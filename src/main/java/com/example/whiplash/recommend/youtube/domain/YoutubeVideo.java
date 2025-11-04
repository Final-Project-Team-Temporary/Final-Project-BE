package com.example.whiplash.recommend.youtube.domain;

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

    private String videoId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private String description;
    private Long viewCount;
    private String publishedAt;
    private String duration;
}
