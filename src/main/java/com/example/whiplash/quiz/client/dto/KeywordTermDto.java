package com.example.whiplash.quiz.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KeywordTermDto {

    private String term;

    @JsonProperty("term_summary")
    private String termSummary;
}
