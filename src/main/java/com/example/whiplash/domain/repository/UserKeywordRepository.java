package com.example.whiplash.domain.repository;

import com.example.whiplash.domain.entity.UserKeyword;
import com.example.whiplash.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    List<UserKeyword> findAllByUserOrderByPriority(User user);
}
