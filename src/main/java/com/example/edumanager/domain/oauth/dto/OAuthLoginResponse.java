package com.example.edumanager.domain.oauth.dto;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthLoginResponse {

    @JsonProperty("isNewUser")
    private final boolean isNewUser;
    private final LoginResponse loginData;
    private final String tempToken;

    @Builder(access = AccessLevel.PRIVATE)
    private OAuthLoginResponse(boolean isNewUser, LoginResponse loginData, String tempToken) {
        this.isNewUser = isNewUser;
        this.loginData = loginData;
        this.tempToken = tempToken;
    }

    public static OAuthLoginResponse existing(LoginResponse loginData) {
        return OAuthLoginResponse.builder()
                .isNewUser(false)
                .loginData(loginData)
                .build();
    }

    public static OAuthLoginResponse newUser(String tempToken) {
        return OAuthLoginResponse.builder()
                .isNewUser(true)
                .tempToken(tempToken)
                .build();
    }
}
