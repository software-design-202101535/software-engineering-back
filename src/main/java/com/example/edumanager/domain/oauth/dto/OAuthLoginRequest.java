package com.example.edumanager.domain.oauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthLoginRequest {

    @NotBlank(message = "code를 입력해주세요.")
    private String code;

    public static OAuthLoginRequest of(String code) {
        OAuthLoginRequest request = new OAuthLoginRequest();
        request.code = code;
        return request;
    }
}
