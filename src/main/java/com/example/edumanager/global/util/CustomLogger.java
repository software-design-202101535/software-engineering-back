package com.example.edumanager.global.util;

import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public class CustomLogger {

    private CustomLogger() {}

    public static ResponseEntity<ErrorResponse> errorLog(String message, ErrorCode errorCode, Exception e) {
        log.error("{}", message, e);
        return ErrorResponse.toResponseEntity(errorCode);
    }

    public static ResponseEntity<ErrorResponse> warnLog(String message, ErrorCode errorCode) {
        log.warn("{}", message);
        return ErrorResponse.toResponseEntity(errorCode);
    }
}
