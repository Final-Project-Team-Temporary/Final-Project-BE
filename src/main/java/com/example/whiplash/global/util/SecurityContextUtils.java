package com.example.whiplash.global.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityContextUtils {
    public static Optional<String> getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //인증안된 친구
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {  //로그인을 하지 않아도 AnonymousAuthentication 객체를 만든다!
            return Optional.empty();
        }

        //인증된 친구
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }

        return Optional.empty();
    }
}
