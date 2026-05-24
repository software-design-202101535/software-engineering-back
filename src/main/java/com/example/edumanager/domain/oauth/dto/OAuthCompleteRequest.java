package com.example.edumanager.domain.oauth.dto;

import com.example.edumanager.domain.user.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OAuthCompleteRequest {

    @NotBlank(message = "authCode를 입력해주세요.")
    private String authCode;

    @NotNull(message = "역할을 선택해주세요.")
    private Role role;

    private String email;

    @Valid
    private TeacherInfo teacherInfo;

    @Valid
    private StudentInfo studentInfo;

    @Valid
    private ParentInfo parentInfo;

    @AssertTrue(message = "이용약관에 동의해주세요.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보처리방침에 동의해주세요.")
    private boolean privacyAgreed;

    @Getter
    public static class TeacherInfo {
        @NotBlank(message = "학교를 선택해주세요.")
        private String school;

        @Min(value = 1, message = "학년을 입력해주세요.")
        private int grade;

        @Min(value = 1, message = "반을 입력해주세요.")
        private int classNum;
    }

    @Getter
    public static class StudentInfo {
        @NotBlank(message = "학교를 선택해주세요.")
        private String school;

        @Min(value = 1, message = "학년을 입력해주세요.")
        private int grade;

        @Min(value = 1, message = "반을 입력해주세요.")
        private int classNum;

        @Min(value = 1, message = "번호를 입력해주세요.")
        private int number;
    }

    @Getter
    public static class ParentInfo {
        @NotBlank(message = "자녀의 이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String childEmail;
    }
}
