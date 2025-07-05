package com.example.whiplash.apiPayload.exception;

import com.example.whiplash.apiPayload.ErrorStatus;
import lombok.Getter;

@Getter
public class WhiplashException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public WhiplashException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
}
