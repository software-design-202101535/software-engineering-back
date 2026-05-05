package com.example.edumanager.global.security.exception;

import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.exception.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.JWT_ENTRY_POINT;

        JwtAuthException jwtException = (JwtAuthException) request.getAttribute("jwtException");
        if (jwtException != null) {
            errorCode = jwtException.getErrorCode();
        }

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        String json = new ObjectMapper().writeValueAsString(
                ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .name(errorCode.name())
                        .message(errorCode.getMessage())
                        .build()
        );
        response.getWriter().write(json);
    }
}
