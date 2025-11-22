package com.example.whiplash.quiz.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StockDto {

    @JsonProperty("stock_name")
    private String stockName;

    @JsonProperty("stock_code")
    private String stockCode;

    private String market;

    private String sector;
}
