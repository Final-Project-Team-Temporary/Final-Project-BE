package com.example.whiplash.apiPayload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
    // 인증 관련
    UNAUTHORIZED("E001", "인증이 필요합니다"),
    INVALID_TOKEN("E002", "유효하지 않은 토큰입니다"),
    INVALID_PASSWORD("E003", "비밀번호가 일치하지 않습니다."),

    // 사용자 관련
    USER_NOT_FOUND("U401", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("4102", "이미 가입된 이메일입니다"),
    USER_ALREADY_ACTIVATED("U403", "이미 활성화된 사용자입니다."),
    USER_NOT_ACTIVATED("U404", "아직 활성화되지 않았거나 비활성화된 사용자입니다."),

    // 검증 관련
    INVALID_INPUT("E201", "입력값이 올바르지 않습니다"),
    VALIDATION_FAILED("E202", "데이터 검증에 실패했습니다"),
    
    // 시스템 관련
    INTERNAL_SERVER_ERROR("E999", "서버 내부 오류가 발생했습니다");

    private final String code;
    private final String message;

}
