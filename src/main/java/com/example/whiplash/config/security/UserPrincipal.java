package com.example.whiplash.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;  // Optional
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.authorities = Collections.emptyList();
    }

    public UserPrincipal(Long userId) {
        this(userId, null);
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);  // UserDetails 구현
    }

    @Override
    public String getPassword() {
        return null;  // JWT 방식에서는 불필요
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
