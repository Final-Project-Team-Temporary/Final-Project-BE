package com.example.whiplash.user.service;

import com.example.whiplash.user.Role;
import com.example.whiplash.user.User;
import com.example.whiplash.user.UserModifyRequestDTO;
import com.example.whiplash.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class UserServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    void init() {
        userRepository.save(User.builder()
                .name("olaf")
                .age(12)
                .email("rmsghchl0@gmail.com")
                .role(Role.USER)
                .build()
        );
        userRepository.save(User.builder()
                .name("olaf2")
                .age(29)
                .email("rmsghchl02@gmail.com")
                .role(Role.USER)
                .build()
        );
        userRepository.save(User.builder()
                .name("olaf3")
                .age(14)
                .email("rmsghchl03@gmail.com")
                .role(Role.USER)
                .build()
        );
    }
    @Test
    void create() {
        //given
        User user = User.builder()
                .name("olaf4")
                .age(14)
                .email("rmsghchl034@gmail.com")
                .role(Role.USER)
                .build();
        //when
        Long userId = userService.create(user);
        em.flush();
        em.clear();

        //then
        User findUser = userService.findById(userId);
        Assertions.assertEquals(user.getEmail(), findUser.getEmail());
    }

    @Test
    void findById() {
        //given
        User user = User.builder()
                .name("olaf4")
                .age(14)
                .email("rmsghchl034@gmail.com")
                .role(Role.USER)
                .build();

        Long userId = userService.create(user);

        //when
        User findUser = userService.findById(userId);

        //then
        Assertions.assertEquals(user.getName(), findUser.getName());
    }

    @Test
    void findAll() {
        //given
        //when
        List<User> users = userService.findAll();

        //then
        Assertions.assertEquals(3, users.size());
    }

    @Test
    void update() {
        //given
        String newName = "good";
        User user = User.builder()
                .name("olaf4")
                .age(14)
                .email("rmsghchl034@gmail.com")
                .role(Role.USER)
                .build();

        Long userId = userService.create(user);

        //when
        userService.update(UserModifyRequestDTO.builder()
                .name(newName)
                .age(14)
                .email("rmsghchl0@gmail.com")
                .build(), userId);
        em.flush();
        em.clear();

        //then
        User findUser = userService.findById(userId);
        Assertions.assertEquals(newName, findUser.getName());

    }

    @Test
    void delete() {
        //given
        User user = User.builder()
                .name("olaf4")
                .age(14)
                .email("rmsghchl034@gmail.com")
                .role(Role.USER)
                .build();

        Long userId = userService.create(user);
        //when
        userService.delete(userId);
        em.flush();
        em.clear();

        //then
        Assertions.assertEquals(3, userService.findAll().size());
    }
}