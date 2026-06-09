package com.example.whiplash.quiz.repository;

import com.example.whiplash.quiz.document.TermQuizPool;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TermQuizPoolRepository extends MongoRepository<TermQuizPool, String> {

    Optional<TermQuizPool> findByTermName(String termName);

    boolean existsByTermName(String termName);
}