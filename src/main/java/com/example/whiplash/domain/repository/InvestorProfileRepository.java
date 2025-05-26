package com.example.whiplash.domain.repository;

import com.example.whiplash.domain.entity.profile.InvestorProfile;
import com.example.whiplash.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, Long> {
    Optional<InvestorProfile> findByUser(User user);
}
