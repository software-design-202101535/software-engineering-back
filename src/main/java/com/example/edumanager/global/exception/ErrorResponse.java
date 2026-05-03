package com.example.edumanager.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Builder
@Getter
public class ErrorResponse {

    private Integer code;
    private String name;
    private String message;
    private Map<String, String> errors;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .name(errorCode.name())
                        .message(errorCode.getMessage())
                        .build());
    }

    public static ResponseEntity<ErrorResponse> validationFailed(Map<String, String> errors) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .name(errorCode.name())
                        .message(errorCode.getMessage())
                        .errors(errors)
                        .build());
    }
}
