package com.example.whiplash.term.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TermSuggestionResponse {
    List<String> suggestions;
}
