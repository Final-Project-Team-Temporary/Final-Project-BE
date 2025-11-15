package com.example.whiplash.recommend.article.application;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.recommend.article.application.dto.ScoredArticle;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.article.repository.ArticleRecommendRedisRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RecommendService {
	private final RecommendStrategy recommendStrategy;
	private final ArticleRepository articleRepository;
	private final UserRepository userRepository;
	private final ArticleRecommendRedisRepository articleRecommendRedisRepository;

	public void recommendArticle(LocalDateTime recommendCreatedAt, LocalDateTime publishedAtAfter) {
		List<Article> articlePool = articleRepository.findAllByPublishedAtAfter(publishedAtAfter);
		List<User> userPool = userRepository.findAll();

		for (User user : userPool) {
			makeRecommendationForUser(user, articlePool, recommendCreatedAt);
		}
	}

	private void makeRecommendationForUser(User user, List<Article> articlePool, LocalDateTime now) {
		List<Article> rankedArticles = articlePool.stream()
			.map(article -> new ScoredArticle(article, recommendStrategy.getScore(article, user)))
			.sorted(Comparator
				.comparing(ScoredArticle::score).reversed()
				.thenComparing(scoredArticle -> scoredArticle.article().getPublishedAt()).reversed()
			)
			.limit(10)
			.map(ScoredArticle::article)
			.toList();

		ArticleRecommendResult recommendResult = new ArticleRecommendResult(rankedArticles, user, now);

		articleRecommendRedisRepository.storeRecommendations(recommendResult);
	}
}