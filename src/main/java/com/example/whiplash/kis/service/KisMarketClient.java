package com.example.whiplash.kis.service;

import com.example.whiplash.kis.dto.response.KisStockPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kis-market", url = "${kis.api.url}")
public interface KisMarketClient {

    /**
     * 주식 현재가 시세 조회
     * TR_ID: FHKST01010100
     */
    @GetMapping("/uapi/domestic-stock/v1/quotations/inquire-price")
    KisStockPriceResponse getStockPrice(
            @RequestHeader("authorization") String accessToken,
            @RequestHeader("appkey") String appKey,
            @RequestHeader("appsecret") String appSecret,
            @RequestHeader("tr_id") String trId,
            @RequestParam("FID_COND_MRKT_DIV_CODE") String marketDivCode, // 시장 구분 (J: 주식)
            @RequestParam("FID_INPUT_ISCD") String stockCode // 종목 코드 (005930)
    );
}
