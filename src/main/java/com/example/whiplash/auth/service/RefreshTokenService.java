package com.example.whiplash.auth.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.config.security.jwt.JwtProperties;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private final JwtProperties jwtProperties;

    private final JwtTokenProvider jwtTokenProvider;

    public void saveRefreshToken(String refreshToken) {

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        String key = "refresh:user:" + userId;

        stringRedisTemplate.opsForValue().set(key, refreshToken);
        stringRedisTemplate.expire(key, jwtProperties.getRefreshTokenExpiration(), TimeUnit.SECONDS);
    }

    public void removeRefreshToken(String userId){

        String key = "refresh:user:" + userId;

        String refreshToken = stringRedisTemplate.opsForValue().get(key);

        if(refreshToken == null){
            throw new WhiplashException(ErrorStatus.INVALID_TOKEN);
        }

        stringRedisTemplate.delete(key);
    }

    public boolean validateRefreshToken(String refreshToken) {

        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new WhiplashException(ErrorStatus.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String key = "refresh:user:" + userId;
        String storedRefreshToken = stringRedisTemplate.opsForValue().get(key);

        // NPE 방지를 위한 null 체크 추가
        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new WhiplashException(ErrorStatus.INVALID_TOKEN);
        }

        // 기존 Refresh Token 삭제 (토큰 재사용 방지)
        removeRefreshToken(userId);

        return true;
    }
}
