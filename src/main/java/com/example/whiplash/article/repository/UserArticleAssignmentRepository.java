package com.example.whiplash.article.repository;

import com.example.whiplash.article.entity.UserArticleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserArticleAssignmentRepository extends JpaRepository<UserArticleAssignment, Long> {
}
