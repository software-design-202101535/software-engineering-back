package com.example.edumanager.domain.oauth.dto;

import lombok.Getter;

@Getter
public class OAuthAuthorizeResult {

    private final String authCode;
    private final boolean needsInfo;
    private final String email;
    private final String name;

    private OAuthAuthorizeResult(String authCode, boolean needsInfo, String email, String name) {
        this.authCode = authCode;
        this.needsInfo = needsInfo;
        this.email = email;
        this.name = name;
    }

    public static OAuthAuthorizeResult of(String authCode, boolean needsInfo, String email, String name) {
        return new OAuthAuthorizeResult(authCode, needsInfo, email, name);
    }
}
