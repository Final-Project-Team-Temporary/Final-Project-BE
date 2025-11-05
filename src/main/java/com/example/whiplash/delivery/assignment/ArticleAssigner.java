package com.example.whiplash.delivery.assignment;

import com.example.whiplash.article.original.domain.entity.UserArticleAssignment;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleAssigner {

    List<UserArticleAssignment> assign(LocalDateTime assignAfterThisTime);
}
