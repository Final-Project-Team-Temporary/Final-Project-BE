package com.example.whiplash.domain.repository;

import com.example.whiplash.domain.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
