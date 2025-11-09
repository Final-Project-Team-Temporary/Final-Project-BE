package com.example.whiplash.user.repository.keyword;

import com.example.whiplash.keyword.user.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByName(String name);
}
