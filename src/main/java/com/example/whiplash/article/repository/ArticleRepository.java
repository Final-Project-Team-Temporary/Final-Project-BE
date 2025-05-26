package com.example.whiplash.article.repository;

import com.example.whiplash.article.document.Article;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, String> {
}
