package com.example.whiplash.config.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private String isser;

    // 테스트용 기본 설정
    public JwtProperties() {
        this.secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        this.accessTokenExpiration = 1800000L; // 30분
        this.refreshTokenExpiration = 604800000L; // 7일
        this.isser = "whiplash";
    }
}
