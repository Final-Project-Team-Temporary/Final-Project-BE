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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Primary
@Component
public class StringMatchRecommend implements RecommendStrategy{

	@Override
	public double getScore(Set<ArticleKeyword> articleKeywords, Set<UserKeyword> userKeywords) {
		List<Keyword> keywordsForArticle = articleKeywords.stream()
			.map(ArticleKeyword::getKeyword)
			.toList();
		List<Keyword> keywordsForUser = userKeywords.stream()
			.map(UserKeyword::getKeyword)
			.toList();

		Set<Keyword> intersection = new HashSet<>(keywordsForArticle);
		intersection.retainAll(keywordsForUser);

		double score = (double)intersection.size() / userKeywords.size();
		log.info("[StringMatchRecommend] 사용자: {}, 기사: {}, score: {}", userKeywords.stream().findFirst().orElseThrow().getUser().getName(), articleKeywords.stream().findFirst().orElseThrow().getArticleId(), score);
		return score;
	}
}
