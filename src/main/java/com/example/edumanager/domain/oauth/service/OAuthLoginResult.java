package com.example.edumanager.domain.oauth.service;

import lombok.Getter;

@Getter
public class OAuthLoginResult {

    private final Long existingUserId;
    private final String tempToken;

    private OAuthLoginResult(Long existingUserId, String tempToken) {
        this.existingUserId = existingUserId;
        this.tempToken = tempToken;
    }

    public boolean isNewUser() {
        return existingUserId == null;
    }

    public static OAuthLoginResult existing(Long userId) {
        return new OAuthLoginResult(userId, null);
    }

    public static OAuthLoginResult newUser(String tempToken) {
        return new OAuthLoginResult(null, tempToken);
    }
}
