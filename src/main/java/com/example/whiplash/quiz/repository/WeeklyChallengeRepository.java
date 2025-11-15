package com.example.whiplash.quiz.repository;

import com.example.whiplash.quiz.entity.WeeklyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyChallengeRepository extends JpaRepository<WeeklyChallenge, Long> {

    /**
     * 특정 주의 챌린지 조회
     */
    Optional<WeeklyChallenge> findByWeekStartDate(LocalDate weekStartDate);

    /**
     * 가장 최근 챌린지 조회
     */
    Optional<WeeklyChallenge> findFirstByOrderByWeekStartDateDesc();

}
