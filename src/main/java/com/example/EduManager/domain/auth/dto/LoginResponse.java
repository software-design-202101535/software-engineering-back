package com.example.EduManager.domain.auth.dto;

import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private final String accessToken;
    @JsonIgnore
    private final String refreshToken;
    private final Long userId;
    private final String email;
    private final String name;
    private final String role;
    private final Integer grade;
    private final Integer classNum;
    private final Long studentId;
    private final List<ChildSummary> children;

    @Builder
    private LoginResponse(String accessToken, String refreshToken,
                          Long userId, String email, String name, String role,
                          Integer grade, Integer classNum,
                          Long studentId, List<ChildSummary> children) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.grade = grade;
        this.classNum = classNum;
        this.studentId = studentId;
        this.children = children;
    }

    public static LoginResponse ofForTest(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static LoginResponse ofTeacher(User user, AuthTokens tokens, TeacherProfile profile) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .grade(profile.getGrade())
                .classNum(profile.getClassNum())
                .build();
    }

    public static LoginResponse ofStudent(User user, AuthTokens tokens, Long studentId) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .studentId(studentId)
                .build();
    }

    public static LoginResponse ofParent(User user, AuthTokens tokens, List<ChildSummary> children) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .children(children)
                .build();
    }
}
