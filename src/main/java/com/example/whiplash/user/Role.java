package com.example.whiplash.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "방문자"),
    TEMP_USER("ROLE_TEMP_USER", "임시 사용자"),
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String value;

    public String getAuthority() {
        return this.key;
    }

    public static Role getDefaultRole() {
        return TEMP_USER;
    }

    public static Role getActiveUserRole() {
        return USER;
    }
}
