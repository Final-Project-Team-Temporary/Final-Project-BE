package com.example.whiplash.log.quiz.repository;

import com.example.whiplash.log.quiz.entity.QuizSolveLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSolveLogRepository extends JpaRepository<QuizSolveLog, Long> {

    List<QuizSolveLog> findByUserId(Long userId);
}
