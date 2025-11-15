package com.example.whiplash.recommend.article.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleRecommendResult {
	private List<Article> articles;
	private User user;
	private LocalDateTime createdAt;
	// private int currentIndex; // 페이징을 위한 커서
}
