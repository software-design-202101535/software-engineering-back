package com.example.EduManager.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StudentRegisterRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "학교를 선택해주세요.")
    private String school;

    @Min(value = 1, message = "학년을 입력해주세요.")
    private int grade;

    @Min(value = 1, message = "반을 입력해주세요.")
    private int classNum;

    @Min(value = 1, message = "번호를 입력해주세요.")
    private int number;

    @AssertTrue(message = "이용약관에 동의해주세요.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보처리방침에 동의해주세요.")
    private boolean privacyAgreed;

    @SuppressWarnings("java:S107")
    public static StudentRegisterRequest of(String email, String password, String passwordConfirm,
                                             String name, String school, int grade, int classNum, int number) {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.email = email;
        request.password = password;
        request.passwordConfirm = passwordConfirm;
        request.name = name;
        request.school = school;
        request.grade = grade;
        request.classNum = classNum;
        request.number = number;
        request.termsAgreed = true;
        request.privacyAgreed = true;
        return request;
    }
}
