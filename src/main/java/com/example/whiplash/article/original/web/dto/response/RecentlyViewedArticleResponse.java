package com.example.whiplash.article.original.web.dto.response;

import lombok.Builder;

@Builder
public record RecentlyViewedArticleResponse(
	String id,
	String title
) {
}
