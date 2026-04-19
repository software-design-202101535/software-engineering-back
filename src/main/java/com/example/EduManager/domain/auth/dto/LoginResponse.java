package com.example.EduManager.domain.auth.dto;

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
    private final UserResponse user;
    private final Long studentId;
    private final List<ChildSummary> children;

    @Builder
    private LoginResponse(String accessToken, String refreshToken, UserResponse user,
                          Long studentId, List<ChildSummary> children) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.studentId = studentId;
        this.children = children;
    }

    public static LoginResponse ofTeacher(User user, AuthTokens tokens) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(UserResponse.of(user))
                .build();
    }

    public static LoginResponse ofStudent(User user, AuthTokens tokens, Long studentId) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(UserResponse.of(user))
                .studentId(studentId)
                .build();
    }

    public static LoginResponse ofParent(User user, AuthTokens tokens, List<ChildSummary> children) {
        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(UserResponse.of(user))
                .children(children)
                .build();
    }
}
