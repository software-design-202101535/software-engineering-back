package com.example.edumanager.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailLoginRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    public static EmailLoginRequest of(String email, String password) {
        EmailLoginRequest request = new EmailLoginRequest();
        request.email = email;
        request.password = password;
        return request;
    }
}
