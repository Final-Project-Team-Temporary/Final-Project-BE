package com.example.whiplash.recommend.article.application;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.keyword.article.domain.ArticleKeyword;
import com.example.whiplash.keyword.article.repository.ArticleKeywordRepository;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Primary
@Component
public class StringMatchRecommend implements RecommendStrategy{
	private final ArticleKeywordRepository articleKeywordRepository;
	private final UserKeywordRepository userKeywordRepository;

	@Override
	public double getScore(Article article, User user) {
		Set<Keyword> articleKeywords = articleKeywordRepository.findByArticleId(article.getId())
			.stream()
			.map(ArticleKeyword::getKeyword)
			.collect(Collectors.toSet());
		Set<Keyword> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user)
			.stream()
			.map(UserKeyword::getKeyword)
			.collect(Collectors.toSet());

		Set<Keyword> intersection = new HashSet<>(articleKeywords);
		intersection.retainAll(userKeywords);

		int score = intersection.size() / userKeywords.size();
		return score;
	}
}
