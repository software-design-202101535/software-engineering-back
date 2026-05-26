package com.example.edumanager.domain.oauth.client;

import lombok.Getter;

@Getter
public class OAuthUserInfo {

    private final String oauthId;
    private final String email;
    private final String name;

    private OAuthUserInfo(String oauthId, String email, String name) {
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
    }

    public static OAuthUserInfo of(String oauthId, String email, String name) {
        return new OAuthUserInfo(oauthId, email, name);
    }
}
