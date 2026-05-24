package com.example.edumanager.domain.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthTokenRequest {

    @NotBlank(message = "authCode를 입력해주세요.")
    private String authCode;

    public static OAuthTokenRequest of(String authCode) {
        OAuthTokenRequest request = new OAuthTokenRequest();
        request.authCode = authCode;
        return request;
    }
}
