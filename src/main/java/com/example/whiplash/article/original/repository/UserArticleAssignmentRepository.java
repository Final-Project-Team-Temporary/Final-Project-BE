package com.example.whiplash.article.original.repository;

import com.example.whiplash.article.original.domain.entity.UserArticleAssignment;
import com.example.whiplash.domain.entity.history.email.EmailSendStatus;
import com.example.whiplash.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserArticleAssignmentRepository extends JpaRepository<UserArticleAssignment, Long> {
    List<UserArticleAssignment> findAllByUser(User user);
    List<UserArticleAssignment> findAllBySendStatus(EmailSendStatus sendStatus);
}
