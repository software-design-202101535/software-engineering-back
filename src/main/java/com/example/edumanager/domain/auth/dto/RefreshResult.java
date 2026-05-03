package com.example.edumanager.domain.auth.dto;

import lombok.Getter;

@Getter
public class RefreshResult {

    private final String accessToken;
    private final String refreshToken;

    private RefreshResult(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static RefreshResult of(String accessToken, String refreshToken) {
        return new RefreshResult(accessToken, refreshToken);
    }
}
