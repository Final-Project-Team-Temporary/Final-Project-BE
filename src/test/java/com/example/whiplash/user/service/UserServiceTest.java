package com.example.whiplash.user.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.example.whiplash.user.domain.profile.AgeRange.TWENTIES;
import static com.example.whiplash.user.domain.profile.InvestmentGoal.EDUCATION_FUND;
import static com.example.whiplash.user.domain.profile.InvestmentLevel.BEGINNER;
import static com.example.whiplash.user.domain.profile.RiskTolerance.AGGRESSIVE;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest extends IntegrationTestSupport {
    @Autowired
    UserService userService;
    @Autowired
    AuthService authService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;

    @DisplayName("기존 유저에게 유저 프로필을 등록한다.")
    @Test
    void should_hasSize_1_when_register_user() {
        //given
        String email = "rmsghchl0@gmail.com";
        String username = "id";
        String password = "pw";
        authService.joinUser(UserCreateDTO.builder()
                .username(username)
                .password(password)
                .email(email)
                .build()
        );
        ProfileRegisterDTO profileRegisterDTO = ProfileRegisterDTO.builder()
                .ageRange(TWENTIES)
                .investmentGoal(EDUCATION_FUND)
                .riskTolerance(AGGRESSIVE)
                .investmentLevel(BEGINNER)
                .build();

        //when
//        User registeredUser = userService.registerProfile(profileRegisterDTO, Optional.of(email));

        //then
//        assertThat(registeredUser).isNotNull()
//                .extracting(User::getEmail,
//                        user -> user.getInvestorProfile().getAgeRange(),
//                        user -> user.getInvestorProfile().getRiskTolerance(),
//                        user -> user.getInvestorProfile().getInvestmentLevel()
//                )
//                .containsExactlyInAnyOrder(email, TWENTIES, AGGRESSIVE, BEGINNER)
        ;
    }

}