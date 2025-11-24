package com.example.whiplash.quiz.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class KeywordExtractionResponse {

    List<KeywordTermDto> results;
}
