package com.example.whiplash.apiPayload.exception;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.apiPayload.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class WhiplashExceptionHandler {

    @ExceptionHandler(WhiplashException.class)
    public ResponseEntity<ApiResponse<Void>> handleWhiplashException(WhiplashException e) {
        return ResponseEntity.badRequest().body(ApiResponse.onFailure(e.getErrorStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.onFailure(ErrorStatus.VALIDATION_FAILED, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception e) {
        return ResponseEntity.internalServerError()
            .body(ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR));
    }
}
