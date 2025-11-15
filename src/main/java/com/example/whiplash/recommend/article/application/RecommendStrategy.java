package com.example.whiplash.recommend.article.application;

import java.util.Set;

import com.example.whiplash.keyword.article.domain.ArticleKeyword;
import com.example.whiplash.keyword.user.UserKeyword;

public interface RecommendStrategy {
	double getScore(Set<ArticleKeyword> articleKeywords, Set<UserKeyword> userKeywords);
}
