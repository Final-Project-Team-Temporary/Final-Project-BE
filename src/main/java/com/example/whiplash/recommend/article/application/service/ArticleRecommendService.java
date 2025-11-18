package com.example.whiplash.recommend.article.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.keyword.article.domain.ArticleKeyword;
import com.example.whiplash.keyword.article.repository.ArticleKeywordRepository;
import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.recommend.article.application.RecommendStrategy;
import com.example.whiplash.recommend.article.application.dto.ScoredArticle;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.article.repository.ArticleRecommendRedisRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleRecommendService {
	private final RecommendStrategy recommendStrategy;
	private final ArticleRepository articleRepository;
	private final UserRepository userRepository;
	private final UserKeywordRepository userKeywordRepository;
	private final ArticleKeywordRepository articleKeywordRepository;
	private final ArticleRecommendRedisRepository articleRecommendRedisRepository;

	public void recommendArticle(LocalDateTime recommendCreatedAt, LocalDateTime publishedAtAfter) {
		Map<Article, List<ArticleKeyword>> articleKeywordMap = getArticleKeywordMap(publishedAtAfter);

		List<User> userPool = userRepository.findAll();

		for (User user : userPool) {
			calculateScoreAndSaveRecommendation(recommendCreatedAt, user, articleKeywordMap);
		}
	}

	private void calculateScoreAndSaveRecommendation(LocalDateTime recommendCreatedAt, User user,
		Map<Article, List<ArticleKeyword>> articleKeywordMap) {
		List<UserKeyword> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user);
		if (userNotRegisterKeywords(userKeywords)) {
			return;
		}

		List<ScoredArticle> scoredArticles = getScoredArticles(userKeywords, articleKeywordMap);

		saveRecommendationForUser(user, scoredArticles, recommendCreatedAt);
	}

	private Map<Article, List<ArticleKeyword>> getArticleKeywordMap(LocalDateTime publishedAtAfter) {
		List<Article> articlePool = articleRepository.findAllByPublishedAtAfter(publishedAtAfter);

		Map<Article, List<ArticleKeyword>> articleKeywordMap = new HashMap<>();
		articlePool.forEach(article -> {
			List<ArticleKeyword> keywordsByArticle = articleKeywordRepository.findByArticleId(article.getId());
			if (!keywordsByArticle.isEmpty()) {
				articleKeywordMap.put(article, keywordsByArticle);
			}
		});

		return articleKeywordMap;
	}

	private static boolean userNotRegisterKeywords(List<UserKeyword> userKeywords) {
		return userKeywords.isEmpty();
	}

	private List<ScoredArticle> getScoredArticles(List<UserKeyword> userKeywords,
		Map<Article, List<ArticleKeyword>> articleKeywordMap
	) {
		List<ScoredArticle> scoredArticles = new ArrayList<>();
		articleKeywordMap.forEach((article, articleKeywords) -> {
			calculateScoreAndAdd(userKeywords, articleKeywords, scoredArticles, article);
		});

		return subListTop10ScoredArticles(scoredArticles);
	}

	private static List<ScoredArticle> subListTop10ScoredArticles(List<ScoredArticle> scoredArticles) {
		Collections.sort(scoredArticles, Comparator
			.comparing(ScoredArticle::score, Comparator.reverseOrder())
			.thenComparing(scoredArticle -> scoredArticle.article().getPublishedAt(), Comparator.reverseOrder())
		);

		return scoredArticles.size() > 10 ? new ArrayList<>(scoredArticles.subList(0, 10)) : scoredArticles;
	}

	private void calculateScoreAndAdd(List<UserKeyword> userKeywords,
		List<ArticleKeyword> articleKeywords,
		List<ScoredArticle> scoredArticles,
		Article article
	) {
		ScoredArticle scoredArticle = new ScoredArticle(
			article, recommendStrategy.getScore(new HashSet<>(articleKeywords), new HashSet<>(userKeywords)));
		scoredArticles.add(scoredArticle);
	}

	private void saveRecommendationForUser(User user, List<ScoredArticle> rankedArticles, LocalDateTime createdAt) {
		ArticleRecommendResult recommendResult = new ArticleRecommendResult(rankedArticles, user, createdAt);
		articleRecommendRedisRepository.storeRecommendations(recommendResult);
	}

}