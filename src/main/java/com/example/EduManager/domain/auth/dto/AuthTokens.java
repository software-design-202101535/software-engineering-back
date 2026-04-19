package com.example.EduManager.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthTokens {

    private final String accessToken;
    private final String refreshToken;

    @Builder
    private AuthTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static AuthTokens of(String accessToken, String refreshToken) {
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
