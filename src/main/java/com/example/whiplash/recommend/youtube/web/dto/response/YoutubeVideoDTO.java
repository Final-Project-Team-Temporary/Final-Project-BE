package com.example.whiplash.recommend.youtube.web.dto.response;

import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;

public record YoutubeVideoDTO(
        String videoId,
        String title,
        String channelTitle,
        String thumbnailUrl,
        String description,
        Long viewCount,
        String publishedAt,
        String duration
) {
    public static YoutubeVideoDTO from(YoutubeVideo video) {
        return new YoutubeVideoDTO(
                video.getVideoId(),
                video.getTitle(),
                video.getChannelTitle(),
                video.getThumbnailUrl(),
                video.getDescription(),
                video.getViewCount(),
                video.getPublishedAt(),
                video.getDuration()
        );
    }
}
