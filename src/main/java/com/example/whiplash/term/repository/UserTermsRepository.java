package com.example.whiplash.term.repository;

import com.example.whiplash.term.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

    @Query("select ut from UserTerms ut join fetch ut.terms t where ut.user.id = :userId")
    List<UserTerms> findByUserId(Long userId);
}
