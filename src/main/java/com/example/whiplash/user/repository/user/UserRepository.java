package com.example.whiplash.user.repository.user;

import com.example.whiplash.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByKakaoId(Long kakaoId);

    /**
     * 활성 사용자 조회 (최근 N일 내 접속한 사용자)
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :threshold ORDER BY u.lastLoginAt DESC")
    List<User> findActiveUsersSince(@Param("threshold") LocalDateTime threshold);

    /**
     * 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :threshold")
    long countActiveUsersSince(@Param("threshold") LocalDateTime threshold);
}
