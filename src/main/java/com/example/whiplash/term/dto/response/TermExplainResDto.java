package com.example.whiplash.term.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TermExplainResDto {
    private String term;
    private String definition;
}
