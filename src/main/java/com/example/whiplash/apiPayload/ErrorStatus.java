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
    FORBIDDEN("E004", "접근 권한이 없습니다"),

    // 사용자 관련
    USER_NOT_FOUND("U401", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("4102", "이미 가입된 이메일입니다"),
    USER_ALREADY_ACTIVATED("U403", "이미 활성화된 사용자입니다."),
    USER_NOT_ACTIVATED("U404", "아직 활성화되지 않았거나 비활성화된 사용자입니다."),
    USER_ALREADY_JOINED("U405", "이미 가입된 유저입니다."),

    // 검증 관련
    INVALID_INPUT("E201", "입력값이 올바르지 않습니다"),
    VALIDATION_FAILED("E202", "데이터 검증에 실패했습니다"),
    
    // 기사 메타정보 관련
    ARTICLE_META_ALREADY_EXISTS("A301", "기사 메타정보가 이미 존재합니다"),
    ARTICLE_META_NOT_FOUND("A302", "기사 메타정보를 찾을 수 없습니다"),
    INVALID_ARTICLE_ID("A303", "유효하지 않은 기사 ID입니다"),
    INVALID_ARTICLE_TITLE("A304", "유효하지 않은 기사 제목입니다"),
    INVALID_ARTICLE_SOURCE("A305", "유효하지 않은 기사 출처입니다"),
    INVALID_ARTICLE_URL("A306", "유효하지 않은 기사 URL입니다"),

    // 기사 조회 관련
    ARTICLE_NOT_FOUND("A401", "기사를 찾을 수 없습니다"),
    SUMMARIZED_ARTICLE_NOT_FOUND("A402", "요약된 기사를 찾을 수 없습니다"),
    INCOMPLETE_ARTICLE_SUMMARIES("A403", "기사의 요약이 완전하지 않습니다"),
    
    // 작업 관련
    JOB_NOT_FOUND("J401", "요청한 작업을 찾을 수 없습니다"),

    // 용어 관련
    TERM_SAVE_FAILED("J402", "용어 저장에 실패했습니다."),
    TERM_ALREADY_IN_DICTIONARY("J403", "이미 용어사전에 저장된 용어입니다."),
    
    // 시스템 관련
    INTERNAL_SERVER_ERROR("E999", "서버 내부 오류가 발생했습니다");

    private final String code;
    private final String message;

}
