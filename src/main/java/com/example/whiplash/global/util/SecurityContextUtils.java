package com.example.whiplash.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Slf4j
public class SecurityContextUtils {

    /**
     * SecurityContext에서 현재 사용자의 ID를 추출합니다.
     * 모든 인증 방식(이메일 로그인, 카카오 로그인)에서 공통적으로 사용 가능합니다.
     * JWT 토큰의 subject에는 항상 User의 DB ID가 저장됩니다.
     *
     * @return 현재 사용자의 ID (Long), 인증되지 않은 경우 Optional.empty()
     */
    public static Optional<Long> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 경우
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // 인증된 경우
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            try {
                String userId = userDetails.getUsername();
                log.debug("Current User ID from SecurityContext: {}", userId);
                return Optional.of(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                log.error("Failed to parse user ID from SecurityContext: {}", userDetails.getUsername(), e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * @deprecated Use getCurrentUserId() instead.
     * 하위 호환성을 위해 유지되지만, getCurrentUserId()를 사용하는 것을 권장합니다.
     */
    @Deprecated
    public static Optional<String> getCurrentUserEmail() {
        log.warn("getCurrentUserEmail() is deprecated. Use getCurrentUserId() instead.");
        return getCurrentUserId().map(String::valueOf);
    }

    /**
     * @deprecated Use getCurrentUserId() instead.
     * 하위 호환성을 위해 유지되지만, getCurrentUserId()를 사용하는 것을 권장합니다.
     */
    @Deprecated
    public static Optional<Long> getCurrentKakaoId() {
        log.warn("getCurrentKakaoId() is deprecated. Use getCurrentUserId() instead.");
        return getCurrentUserId();
    }

}
