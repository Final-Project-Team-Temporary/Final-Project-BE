package com.example.whiplash.term.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DictionaryTermListResDto {
    private Long userTermId;
    private String termName;
    private String termDescription;
    private LocalDateTime createdAt;
}
