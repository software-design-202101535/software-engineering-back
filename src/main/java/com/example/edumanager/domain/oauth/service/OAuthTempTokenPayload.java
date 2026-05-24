package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import lombok.Getter;

@Getter
public class OAuthTempTokenPayload {

    private final String oauthId;
    private final OAuthProvider provider;
    private final String email;
    private final String name;

    private OAuthTempTokenPayload(String oauthId, OAuthProvider provider, String email, String name) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
    }

    public static OAuthTempTokenPayload of(String oauthId, OAuthProvider provider, String email, String name) {
        return new OAuthTempTokenPayload(oauthId, provider, email, name);
    }
}
