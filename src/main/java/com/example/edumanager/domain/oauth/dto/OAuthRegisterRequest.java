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
public class OAuthRegisterRequest {

    @NotBlank(message = "임시 토큰을 입력해주세요.")
    private String tempToken;

    @NotNull(message = "역할을 선택해주세요.")
    private Role role;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

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

    @SuppressWarnings("java:S107")
    public static OAuthRegisterRequest of(String tempToken, Role role, String email, String name,
                                           TeacherInfo teacherInfo, StudentInfo studentInfo, ParentInfo parentInfo) {
        OAuthRegisterRequest request = new OAuthRegisterRequest();
        request.tempToken = tempToken;
        request.role = role;
        request.email = email;
        request.name = name;
        request.teacherInfo = teacherInfo;
        request.studentInfo = studentInfo;
        request.parentInfo = parentInfo;
        request.termsAgreed = true;
        request.privacyAgreed = true;
        return request;
    }

    @Getter
    public static class TeacherInfo {
        @NotBlank(message = "학교를 선택해주세요.")
        private String school;

        @Min(value = 1, message = "학년을 입력해주세요.")
        private int grade;

        @Min(value = 1, message = "반을 입력해주세요.")
        private int classNum;

        public static TeacherInfo of(String school, int grade, int classNum) {
            TeacherInfo info = new TeacherInfo();
            info.school = school;
            info.grade = grade;
            info.classNum = classNum;
            return info;
        }
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

        public static StudentInfo of(String school, int grade, int classNum, int number) {
            StudentInfo info = new StudentInfo();
            info.school = school;
            info.grade = grade;
            info.classNum = classNum;
            info.number = number;
            return info;
        }
    }

    @Getter
    public static class ParentInfo {
        @NotBlank(message = "자녀의 이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String childEmail;

        public static ParentInfo of(String childEmail) {
            ParentInfo info = new ParentInfo();
            info.childEmail = childEmail;
            return info;
        }
    }
}
