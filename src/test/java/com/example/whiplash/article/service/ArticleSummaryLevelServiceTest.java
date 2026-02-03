package com.example.whiplash.article.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.summary.service.ArticleSummaryLevelService;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.SummaryLevelUpdateRequest;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;
import com.example.whiplash.user.web.dto.response.SummaryLevelUpdateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ArticleSummaryLevelService 테스트")
class ArticleSummaryLevelServiceTest extends IntegrationTestSupport {

    @Autowired
	ArticleSummaryLevelService articleSummaryLevelService;

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @DisplayName("인증된 사용자의 요약 레벨을 성공적으로 업데이트한다")
    @Test
    void should_updateSummaryLevel_when_authenticatedUserProvided() {
        // given
        String email = "test@example.com";
        String username = "testuser";
        String password = "password123";

        authService.joinUser(UserCreateDTO.builder()
                .username(username)
                .password(password)
                .email(email)
                .build()
        );

        SummaryLevelUpdateRequest request = new SummaryLevelUpdateRequest(SummaryLevel.ADVANCED);

        // when
        SummaryLevelUpdateResponse response = articleSummaryLevelService.updateSummaryLevel(request, Optional.of(email));

        // then
        assertThat(response).isNotNull()
                .satisfies(res -> {
                    assertThat(res.userId()).isNotNull();
                    assertThat(res.summaryLevel()).isEqualTo(SummaryLevel.ADVANCED);
                });

        User updatedUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(updatedUser.getSummaryLevel()).isEqualTo(SummaryLevel.ADVANCED);
    }

    @DisplayName("인증되지 않은 사용자의 요약 레벨 업데이트 시도 시 예외를 던진다")
    @Test
    void should_throwUnauthorizedException_when_unauthenticatedUser() {
        // given
        SummaryLevelUpdateRequest request = new SummaryLevelUpdateRequest(SummaryLevel.MEDIUM);

        // when & then
        assertThatThrownBy(() -> articleSummaryLevelService.updateSummaryLevel(request, Optional.empty()))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.UNAUTHORIZED);
                });
    }

    @DisplayName("존재하지 않는 사용자의 요약 레벨 업데이트 시도 시 예외를 던진다")
    @Test
    void should_throwUserNotFoundException_when_userNotFound() {
        // given
        String nonExistentEmail = "nonexistent@example.com";
        SummaryLevelUpdateRequest request = new SummaryLevelUpdateRequest(SummaryLevel.EASY);

        // when & then
        assertThatThrownBy(() -> articleSummaryLevelService.updateSummaryLevel(request, Optional.of(nonExistentEmail)))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
                });
    }
}
