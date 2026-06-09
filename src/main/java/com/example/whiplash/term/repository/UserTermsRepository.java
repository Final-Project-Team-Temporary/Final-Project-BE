package com.example.whiplash.term.repository;

import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

    @Query(value = "SELECT ut FROM UserTerms ut JOIN FETCH ut.terms t WHERE ut.user.id = :userId",
           countQuery = "SELECT COUNT(ut) FROM UserTerms ut WHERE ut.user.id = :userId")
    Page<UserTerms> findByUserId(Long userId, Pageable pageable);

    List<UserTerms> findByUserId(Long userId);

    boolean existsByUserAndTerms(User user, Terms terms);

    @Query("SELECT ut FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND ut.createdAt BETWEEN :startDate AND :endDate")
    List<UserTerms> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 용어명 부분 검색 (LIKE '%keyword%')
     * Full-text index가 없는 환경에서도 동작하도록 LIKE로 변경
     */
    @Query("SELECT ut FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND LOWER(ut.terms.termName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY ut.createdAt DESC")
    Page<UserTerms> searchByTermContaining(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 용어명 전방 일치 검색 — 자동완성용
     */
    @Query("SELECT ut FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND LOWER(ut.terms.termName) LIKE LOWER(CONCAT(:keyword, '%')) " +
            "ORDER BY ut.createdAt DESC")
    Page<UserTerms> searchByTermStartsWith(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 용어명 자동완성 제안 (중복 제거, 최대 10개)
     */
    @Query("SELECT DISTINCT ut.terms.termName FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND LOWER(ut.terms.termName) LIKE LOWER(CONCAT(:keyword, '%')) " +
            "ORDER BY ut.terms.termName ASC")
    List<String> findTermSuggestions(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 전체 사용자에 걸쳐 고유 termName 목록 반환 — 배치 처리용
     */
    @Query("SELECT DISTINCT ut.terms.termName FROM UserTerms ut")
    List<String> findDistinctTermNames();
}