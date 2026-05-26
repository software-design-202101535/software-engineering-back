package com.example.edumanager.domain.oauth.service;

import lombok.Getter;

@Getter
public class OAuthLoginResult {

    private final Long existingUserId;
    private final String tempToken;
    private final String email;
    private final String name;

    private OAuthLoginResult(Long existingUserId, String tempToken, String email, String name) {
        this.existingUserId = existingUserId;
        this.tempToken = tempToken;
        this.email = email;
        this.name = name;
    }

    public boolean isNewUser() {
        return existingUserId == null;
    }

    public static OAuthLoginResult existing(Long userId) {
        return new OAuthLoginResult(userId, null, null, null);
    }

    public static OAuthLoginResult newUser(String tempToken, String email, String name) {
        return new OAuthLoginResult(null, tempToken, email, name);
    }
}
