package com.example.whiplash.user.repository;

import com.example.whiplash.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
