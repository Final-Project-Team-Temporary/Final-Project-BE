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

    @Query("select ut from UserTerms ut join fetch ut.terms t where ut.user.id = :userId")
    List<UserTerms> findByUserId(Long userId);

    // 중복 체크용
    boolean existsByUserAndTerms(User user, Terms terms);

    /**
     * 특정 기간에 저장한 용어 조회
     */
    @Query("SELECT ut FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND ut.createdAt BETWEEN :startDate AND :endDate")
    List<UserTerms> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * ⭐ 용어명 부분 검색 (LIKE '%keyword%')
     * 예: "금" 검색 → "금리", "금융", "환금성"
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
     * ⭐ 용어명 시작 검색 (LIKE 'keyword%') - 자동완성용
     * 예: "금" 검색 → "금리", "금융" (✅), "환금성" (❌)
     */
    @Query("SELECT ut FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND LOWER(ut.terms.termName) LIKE LOWER(CONCAT(:keyword, '%')) " +
            "ORDER BY ut.createdAt DESC")
    List<UserTerms> searchByTermStartsWith(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * ⭐ 용어명 자동완성 (중복 제거, 최대 10개)
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
}
