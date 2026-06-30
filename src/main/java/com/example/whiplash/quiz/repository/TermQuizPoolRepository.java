package com.example.whiplash.quiz.repository;

import com.example.whiplash.quiz.document.TermQuizPool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TermQuizPoolRepository extends MongoRepository<TermQuizPool, String> {

    Optional<TermQuizPool> findByTermName(String termName);

    boolean existsByTermName(String termName);

    /** term_name 필드만 조회 — 배치 bulk 존재 확인용. quizzes 필드는 제외한다. */
    @Query(value = "{}", fields = "{'term_name': 1, '_id': 0}")
    List<TermQuizPool> findAllWithTermNameOnly();
}