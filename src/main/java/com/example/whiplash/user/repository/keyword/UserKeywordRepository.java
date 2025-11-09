package com.example.whiplash.user.repository.keyword;

import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    List<UserKeyword> findAllByUserOrderByPriority(User user);
}
