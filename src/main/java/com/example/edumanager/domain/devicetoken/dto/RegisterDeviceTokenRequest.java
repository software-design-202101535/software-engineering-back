package com.example.edumanager.domain.devicetoken.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "FCM 토큰을 입력해주세요.")
    private String token;

    public static RegisterDeviceTokenRequest of(String token) {
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest();
        request.token = token;
        return request;
    }
}
