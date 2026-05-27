package com.example.edumanager.domain.oauth.service;

import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import lombok.Getter;

@Getter
public class OAuthTempTokenPayload {

    private final String oauthId;
    private final OAuthProvider provider;

    private OAuthTempTokenPayload(String oauthId, OAuthProvider provider) {
        this.oauthId = oauthId;
        this.provider = provider;
    }

    public static OAuthTempTokenPayload of(String oauthId, OAuthProvider provider) {
        return new OAuthTempTokenPayload(oauthId, provider);
    }
}
