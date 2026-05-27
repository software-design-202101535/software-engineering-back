package com.example.edumanager.domain.oauth.client;

import lombok.Getter;

@Getter
public class OAuthUserInfo {

    private final String oauthId;

    private OAuthUserInfo(String oauthId) {
        this.oauthId = oauthId;
    }

    public static OAuthUserInfo of(String oauthId) {
        return new OAuthUserInfo(oauthId);
    }
}
