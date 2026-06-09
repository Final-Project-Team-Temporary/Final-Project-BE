package com.example.whiplash.kis.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
public class KisStockPriceResponse {

    @JsonProperty("output")
    private Output output;

    @Data
    public static class Output {
        // 주식 현재가
        @JsonProperty("stck_prpr")
        private String currentPrice;

        // 전일 대비 (부호 포함 안됨, 별도 sign 필드 있음)
        @JsonProperty("prdy_vrss")
        private String priceChange;

        // 전일 대비율 (예: -1.23)
        @JsonProperty("prdy_ctrt")
        private String priceChangeRate;

        // 누적 거래량
        @JsonProperty("acml_vol")
        private String accumulatedVolume;

        // 시가
        @JsonProperty("stck_oprc")
        private String openPrice;

        // 고가
        @JsonProperty("stck_hgpr")
        private String highPrice;

        // 저가
        @JsonProperty("stck_lwpr")
        private String lowPrice;
    }
}
