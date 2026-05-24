package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import lombok.Getter;

@Getter
public class OAuthPending {

    private final OAuthProvider provider;
    private final String oauthId;
    private final String email;
    private final String name;
    private final Long existingUserId;

    private OAuthPending(OAuthProvider provider, String oauthId, String email, String name, Long existingUserId) {
        this.provider = provider;
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.existingUserId = existingUserId;
    }

    public static OAuthPending forExistingUser(OAuthProvider provider, String oauthId, Long userId, String email, String name) {
        return new OAuthPending(provider, oauthId, email, name, userId);
    }

    public static OAuthPending forNewUser(OAuthProvider provider, String oauthId, String email, String name) {
        return new OAuthPending(provider, oauthId, email, name, null);
    }

    public boolean isNewUser() {
        return existingUserId == null;
    }
}
