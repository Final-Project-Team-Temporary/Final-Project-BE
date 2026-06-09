package com.example.whiplash.kis.service;

import com.example.whiplash.kis.dto.response.KisStockPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisMarketService {

    private final KisMarketClient kisMarketClient;
    private final KisTokenService kisTokenService;

    @Value("${kis.app-key}")
    private String appKey;

    @Value("${kis.app-secret}")
    private String appSecret;

    /**
     * 특정 종목의 현재가 정보 조회
     * @param stockCode 종목코드 (예: 005930)
     */
    public KisStockPriceResponse.Output getStockInfo(String stockCode) {
        String accessToken = kisTokenService.getAccessToken(); // Redis에서 토큰 조회

        try {
            KisStockPriceResponse response = kisMarketClient.getStockPrice(
                    accessToken,
                    appKey,
                    appSecret,
                    "FHKST01010100", // 주식 현재가 시세 TR ID
                    "J",             // 주식 시장 구분 코드 (J)
                    stockCode
            );

            if (response == null || response.getOutput() == null) {
                throw new RuntimeException("KIS API 응답이 올바르지 않습니다.");
            }

            log.info("종목코드: {}, 현재가: {}", stockCode, response.getOutput().getCurrentPrice());
            return response.getOutput();

        } catch (Exception e) {
            log.error("시세 조회 실패: {}", e.getMessage());
            // 실제 운영에선 커스텀 Exception을 던져 GlobalExceptionHandler에서 처리
            throw new RuntimeException("주식 시세 조회 중 오류 발생", e);
        }
    }
}
