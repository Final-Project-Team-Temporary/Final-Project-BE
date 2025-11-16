package com.example.whiplash.recommend.article.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.example.whiplash.recommend.article.application.dto.ScoredArticle;
import com.example.whiplash.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRecommendResult {
	private List<ScoredArticle> scoredArticles;
	private User user;
	private LocalDateTime createdAt;
	// private int currentIndex; // 페이징을 위한 커서
}
