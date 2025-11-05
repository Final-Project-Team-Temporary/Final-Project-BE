package com.example.whiplash.recommend.youtube.streams.dto;

import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Redis Streams에서 수신하는 유튜브 추천 메시지
 */
public record YoutubeRecommendStreamMessage(
        @JsonProperty("keywordId")
        String keywordId,

        @JsonProperty("videos")
        List<YoutubeVideo> videos
) {
    public Long getKeywordIdAsLong() {
        return Long.parseLong(keywordId);
    }
}
