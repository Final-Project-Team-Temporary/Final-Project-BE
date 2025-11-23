package com.example.whiplash.bookmark.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BookmarkStatusResDto {

    private Long bookmarkId;

    private boolean isBookmarked;
}
