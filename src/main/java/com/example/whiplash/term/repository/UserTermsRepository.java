package com.example.whiplash.term.repository;

import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.user.domain.User;
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
}
