package com.example.whiplash.apiPayload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessStatus {
    _OK("S001", "성공"),
    _CREATED("S002", "생성 완료");

    private final String code;
    private final String message;
}
