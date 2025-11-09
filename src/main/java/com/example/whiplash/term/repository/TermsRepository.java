package com.example.whiplash.term.repository;

import com.example.whiplash.term.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.core.query.Term;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    Optional<Terms> findByTermName(String termName);
}
