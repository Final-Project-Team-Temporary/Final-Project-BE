package com.example.whiplash.kis.service;

import com.example.whiplash.kis.dto.request.KisTokenRequest;
import com.example.whiplash.kis.dto.response.KisTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisTokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final KisAuthClient kisAuthClient;

    private static final String TOKEN_KEY = "KIS:ACCESS_TOKEN";
    private static final String LOCK_KEY = "KIS:TOKEN_REFRESH_LOCK";
    private static final long SAFETY_MARGIN_SECONDS = 600; // 10분 안전 마진

    @Value("${kis.app-key}")
    private String appKey;

    @Value("${kis.app-secret}")
    private String appSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void initToken() {
        if (!hasValidToken()) {
            log.info(">>>> [Init] 서버 시작: 유효한 토큰이 없어 발급을 시도합니다.");
            refreshAccessToken();
        } else {
            log.info(">>>> [Init] 서버 시작: 이미 유효한 토큰이 존재합니다.");
        }
    }

    @Scheduled(cron = "0 30 07 * * *")
    public void scheduledTokenRefresh() {
        log.info(">>>> [Schedule] 자동 갱신 스케줄러 실행");
        refreshAccessToken();
    }

    public String getAccessToken() {
        String token = stringRedisTemplate.opsForValue().get(TOKEN_KEY);

        if (token == null) {
            // 방어 로직: 정말 만약에 토큰이 없다면 동기적으로라도 받아와야 함
            refreshAccessToken();
            return stringRedisTemplate.opsForValue().get(TOKEN_KEY);
        }

        return token;
    }

    public String refreshAccessToken() {

        KisTokenRequest request = KisTokenRequest.builder()
                .appKey(appKey)
                .appSecret(appSecret)
                .grantType("client_credentials")
                .build();

        try{
            KisTokenResponse response = kisAuthClient.issueToken(request);

            String fullToken = "Bearer " + response.getAccessToken();
            long expiresIn = response.getExpiresIn();

            // Redis 저장 (유효기간에서 10분을 뺀 시간만큼만 캐싱하여 만료 전 갱신 유도)
            stringRedisTemplate.opsForValue().set(
                    TOKEN_KEY,
                    fullToken,
                    expiresIn - SAFETY_MARGIN_SECONDS,
                    TimeUnit.SECONDS
            );

            log.info("KIS Access Token 갱신 완료. 만료일시: {}", response.getExpiredAt());
            return fullToken;
        }catch (Exception e){
            log.error("KIS 토큰 발급 중 오류 발생", e);
            throw new RuntimeException("KIS 토큰 발급 실패", e);
        }
    }

    public Boolean hasValidToken() {
        return stringRedisTemplate.hasKey(TOKEN_KEY);
    }
}
