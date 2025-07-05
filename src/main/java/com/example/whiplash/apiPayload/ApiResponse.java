package com.example.whiplash.apiPayload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private boolean success;

    private String message;

    private String code;

    private T result;

    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>(true, SuccessStatus._OK.getCode(), SuccessStatus._OK.getMessage(), result);
    }

    public static <T> ApiResponse<T> onCreated(T result) {
        return new ApiResponse<>(true, SuccessStatus._CREATED.getCode(), SuccessStatus._CREATED.getMessage(), result);
    }

    public static <T> ApiResponse<T> onFailure(ErrorStatus errorStatus) {
        return new ApiResponse<>(false, errorStatus.getMessage(), errorStatus.getCode(), null);
    }
    
    public static <T> ApiResponse<T> onFailure(ErrorStatus errorStatus, T data) {
        return new ApiResponse<>(false, errorStatus.getMessage(), errorStatus.getCode(), data);
    }
}
