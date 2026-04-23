package com.example.EduManager.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SchoolLoginRequest {

    @NotBlank(message = "학교를 선택해주세요.")
    private String school;

    @NotBlank(message = "학번/사번을 입력해주세요.")
    private String schoolNumber;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
