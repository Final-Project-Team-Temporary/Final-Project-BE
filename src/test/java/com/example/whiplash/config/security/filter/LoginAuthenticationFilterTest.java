package com.example.whiplash.config.security.filter;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.SocialProvider;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("로그인 인증 필터 통합 테스트")
@AutoConfigureMockMvc
class LoginAuthenticationFilterTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유효한 사용자 정보로 로그인하면 액세스 토큰과 리프레시 토큰을 발급받는다")
    void should_returnTokens_when_validCredentialsProvided() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name("테스트사용자")
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .summaryLevel(SummaryLevel.BASIC)
                .socialProvider(SocialProvider.LOCAL)
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.userStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 인증 실패 응답을 반환한다")
    void should_returnUnauthorized_when_userNotFound() throws Exception {
        // given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 인증 실패 응답을 반환한다")
    void should_returnUnauthorized_when_invalidPassword() throws Exception {
        // given
        String email = "test@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name("테스트사용자")
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .summaryLevel(SummaryLevel.BASIC)
                .socialProvider(SocialProvider.LOCAL)
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(wrongPassword);

        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("비활성화된 사용자가 로그인하면 인증 실패 응답을 반환한다")
    void should_returnUnauthorized_when_userNotActivated() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .name("테스트사용자")
                .role(Role.USER)
                .userStatus(UserStatus.PENDING)
                .summaryLevel(SummaryLevel.BASIC)
                .socialProvider(SocialProvider.LOCAL)
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
