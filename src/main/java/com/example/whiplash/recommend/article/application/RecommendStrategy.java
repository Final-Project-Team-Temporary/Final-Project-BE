package com.example.whiplash.recommend.article.application;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.user.domain.User;

public interface RecommendStrategy {
	double getScore(Article article, User user);
}
