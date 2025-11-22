package com.example.whiplash.quiz.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StockExtractionResponse {

    @JsonProperty("matched_stocks")
    List<StockDto> matchedStocks;
}
