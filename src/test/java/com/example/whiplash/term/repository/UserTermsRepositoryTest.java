package com.example.whiplash.term.repository;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.user.domain.LoginStatus;
import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTermsRepositoryTest extends IntegrationTestSupport {

    @Autowired private UserTermsRepository userTermsRepository;
    @Autowired private TermsRepository termsRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .password("pw")
                .role(Role.USER)
                .build());

        saveTerm("ETF", "상장지수펀드");
        saveTerm("금리", "이자율");
        saveTerm("PER", "주가수익비율");
        saveTerm("환율", "외환비율");
    }

    private void saveTerm(String name, String desc) {
        Terms term = termsRepository.save(Terms.builder()
                .termName(name)
                .AiExplanation(desc)
                .build());
        userTermsRepository.save(UserTerms.builder()
                .user(testUser)
                .terms(term)
                .build());
    }

    // ───────────────────────────────────────────────
    // searchByTermContaining
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("searchByTermContaining은 부분 일치하는 용어를 반환한다")
    void searchByTermContaining_returnsPartialMatches() {
        Page<UserTerms> result = userTermsRepository.searchByTermContaining(
                testUser.getId(), "율", PageRequest.of(0, 10));

        // "환율", "PER(주가수익비율)" → termName 기준: 환율만 해당
        List<String> names = result.getContent().stream()
                .map(ut -> ut.getTerms().getTermName())
                .toList();

        assertThat(names).contains("환율");
    }

    @Test
    @DisplayName("searchByTermContaining은 대소문자를 구분하지 않는다")
    void searchByTermContaining_isCaseInsensitive() {
        Page<UserTerms> result = userTermsRepository.searchByTermContaining(
                testUser.getId(), "etf", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTerms().getTermName()).isEqualTo("ETF");
    }

    @Test
    @DisplayName("searchByTermContaining은 일치하는 용어가 없으면 빈 페이지를 반환한다")
    void searchByTermContaining_returnsEmpty_whenNoMatch() {
        Page<UserTerms> result = userTermsRepository.searchByTermContaining(
                testUser.getId(), "없는용어xyz", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    // ───────────────────────────────────────────────
    // findTermSuggestions (자동완성)
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("findTermSuggestions는 전방 일치하는 용어명만 반환한다")
    void findTermSuggestions_returnsPrefixMatches() {
        List<String> suggestions = userTermsRepository.findTermSuggestions(
                testUser.getId(), "E", PageRequest.of(0, 10));

        assertThat(suggestions).containsExactly("ETF");
    }

    @Test
    @DisplayName("findTermSuggestions는 중복 없이 반환한다")
    void findTermSuggestions_returnsDistinct() {
        // 동일 용어를 다른 사용자가 갖고 있어도 이 사용자 기준 1개여야 함
        List<String> suggestions = userTermsRepository.findTermSuggestions(
                testUser.getId(), "금", PageRequest.of(0, 10));

        long distinctCount = suggestions.stream().distinct().count();
        assertThat(distinctCount).isEqualTo(suggestions.size());
    }

    // ───────────────────────────────────────────────
    // findDistinctTermNames (배치용)
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("findDistinctTermNames는 전체 사용자에 걸쳐 고유 termName 목록을 반환한다")
    void findDistinctTermNames_returnsUniqueTermNamesAcrossAllUsers() {
        // given: 두 번째 사용자가 ETF를 중복 저장
        User anotherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password("pw")
                .role(Role.USER)
                .build());
        Terms etf = termsRepository.findByTermName("ETF").orElseThrow();
        userTermsRepository.save(UserTerms.builder().user(anotherUser).terms(etf).build());

        // when
        List<String> distinct = userTermsRepository.findDistinctTermNames();

        // then: ETF가 두 사용자에게 있어도 목록에는 1번만
        long etfCount = distinct.stream().filter("ETF"::equals).count();
        assertThat(etfCount).isEqualTo(1);
        assertThat(distinct).containsExactlyInAnyOrder("ETF", "금리", "PER", "환율");
    }

    @Test
    @DisplayName("저장된 용어가 없으면 빈 목록을 반환한다")
    void findDistinctTermNames_returnsEmpty_whenNoUserTerms() {
        userTermsRepository.deleteAll();

        List<String> result = userTermsRepository.findDistinctTermNames();

        assertThat(result).isEmpty();
    }
}