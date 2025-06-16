package com.example.whiplash.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "방문자"), USER("ROLE_USER","사용자"), ADMIN("ROLE_ADMIN","관리자");

    private final String key;
    private final String value;
}
