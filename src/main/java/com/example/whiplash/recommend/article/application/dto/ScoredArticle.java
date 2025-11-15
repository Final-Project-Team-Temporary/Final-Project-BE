package com.example.whiplash.recommend.article.application.dto;

import com.example.whiplash.article.original.domain.document.Article;

public record ScoredArticle(
	Article article,
	double score
) {
}
