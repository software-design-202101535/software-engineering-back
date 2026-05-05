package com.example.edumanager.domain.auth.dto;

import lombok.Getter;

@Getter
public class TokenRefreshResponse {

    private final String accessToken;

    private TokenRefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public static TokenRefreshResponse of(String accessToken) {
        return new TokenRefreshResponse(accessToken);
    }
}
