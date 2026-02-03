package com.example.whiplash.user.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.user.repository.keyword.KeywordRepository;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;
import com.example.whiplash.user.web.dto.request.UserKeywordBulkCreateRequest;
import com.example.whiplash.user.web.dto.request.UserKeywordDeleteRequest;
import com.example.whiplash.user.web.dto.response.UserKeywordBulkCreateResponse;
import com.example.whiplash.user.web.dto.response.UserKeywordListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserKeywordService 테스트")
class UserKeywordServiceTest extends IntegrationTestSupport {

    @Autowired
    UserKeywordService userKeywordService;

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserKeywordRepository userKeywordRepository;

    @Autowired
    KeywordRepository keywordRepository;

    @DisplayName("사용자의 키워드 목록을 성공적으로 조회한다")
    @Test
    void should_getUserKeywords_when_authenticatedUser() {
        // given
        String email = "keyword_test@example.com";
        authService.joinUser(UserCreateDTO.builder()
                .username("keyworduser")
                .password("password123")
                .email(email)
                .build()
        );

        User user = userRepository.findByEmail(email).orElseThrow();

        Keyword keyword1 = keywordRepository.save(Keyword.create("테슬라"));
        Keyword keyword2 = keywordRepository.save(Keyword.create("애플"));

        userKeywordRepository.save(UserKeyword.create(user, keyword1));
        userKeywordRepository.save(UserKeyword.create(user, keyword2));

        // when
        UserKeywordListResponse response = userKeywordService.getUserKeywords(Optional.of(email));

        // then
        assertThat(response.keywords()).hasSize(2)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("테슬라", "애플");
    }

    @DisplayName("사용자가 키워드를 벌크로 성공적으로 등록한다")
    @Test
    void should_createUserKeywords_when_validKeywordListProvided() {
        // given
        String email = "bulkcreate@example.com";
        authService.joinUser(UserCreateDTO.builder()
                .username("bulkuser")
                .password("password123")
                .email(email)
                .build()
        );

        UserKeywordBulkCreateRequest request = new UserKeywordBulkCreateRequest(
                java.util.List.of("삼성전자", "SK하이닉스", "네이버")
        );

        // when
        UserKeywordBulkCreateResponse response = userKeywordService.createUserKeywords(request, Optional.of(email));

        // then
        assertThat(response.createdCount()).isEqualTo(3);
        assertThat(response.keywords()).hasSize(3)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("삼성전자", "SK하이닉스", "네이버");
    }

    @DisplayName("사용자가 자신의 키워드를 벌크로 성공적으로 삭제한다")
    @Test
    void should_deleteUserKeywords_when_ownKeywordsProvided() {
        // given
        String email = "bulkdelete@example.com";
        authService.joinUser(UserCreateDTO.builder()
                .username("deleteuser")
                .password("password123")
                .email(email)
                .build()
        );

        User user = userRepository.findByEmail(email).orElseThrow();

        Keyword keyword1 = keywordRepository.save(Keyword.create("LG전자"));
        Keyword keyword2 = keywordRepository.save(Keyword.create("현대차"));

        UserKeyword uk1 = userKeywordRepository.save(UserKeyword.create(user, keyword1));
        UserKeyword uk2 = userKeywordRepository.save(UserKeyword.create(user, keyword2));

        UserKeywordDeleteRequest request = new UserKeywordDeleteRequest(
                java.util.List.of(uk1.getId(), uk2.getId())
        );

        // when
        userKeywordService.deleteUserKeywords(request, Optional.of(email));

        // then
        assertThat(userKeywordRepository.findAllById(java.util.List.of(uk1.getId(), uk2.getId())))
                .isEmpty();
    }

    @DisplayName("다른 사용자의 키워드 삭제 시도 시 예외를 던진다")
    @Test
    void should_throwForbiddenException_when_deletingOtherUsersKeywords() {
        // given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        authService.joinUser(UserCreateDTO.builder()
                .username("user1")
                .password("password123")
                .email(email1)
                .build()
        );

        authService.joinUser(UserCreateDTO.builder()
                .username("user2")
                .password("password123")
                .email(email2)
                .build()
        );

        User user1 = userRepository.findByEmail(email1).orElseThrow();
        User user2 = userRepository.findByEmail(email2).orElseThrow();

        Keyword keyword = keywordRepository.save(Keyword.create("카카오"));
        UserKeyword uk1 = userKeywordRepository.save(UserKeyword.create(user1, keyword));

        UserKeywordDeleteRequest request = new UserKeywordDeleteRequest(
                java.util.List.of(uk1.getId())
        );

        // when & then
        assertThatThrownBy(() -> userKeywordService.deleteUserKeywords(request, Optional.of(email2)))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.FORBIDDEN);
                });
    }
}
