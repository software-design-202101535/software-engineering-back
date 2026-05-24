package com.example.edumanager.facade;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.ChildSummary;
import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.auth.service.AuthService;
import com.example.edumanager.domain.oauth.client.OAuthProperties;
import com.example.edumanager.domain.oauth.dto.OAuthAuthorizeResult;
import com.example.edumanager.domain.oauth.dto.OAuthCompleteRequest;
import com.example.edumanager.domain.oauth.dto.OAuthTokenRequest;
import com.example.edumanager.domain.oauth.service.OAuthPending;
import com.example.edumanager.domain.oauth.service.OAuthService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.util.EnumConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthService oauthService;
    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AuthService authService;
    private final OAuthProperties oauthProperties;

    public URI buildKakaoAuthorizeUrl() {
        OAuthProperties.Kakao kakao = oauthProperties.getKakao();
        return UriComponentsBuilder.fromUriString(kakao.getAuthorizationUri())
                .queryParam("client_id", kakao.getClientId())
                .queryParam("redirect_uri", kakao.getRedirectUri())
                .queryParam("response_type", "code")
                .build()
                .toUri();
    }

    public URI buildFrontendRedirectUrl(String code) {
        OAuthAuthorizeResult result = oauthService.handleKakaoCallback(code);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(oauthProperties.getKakao().getFrontendRedirectUri())
                .queryParam("authCode", result.getAuthCode());

        if (result.isNeedsInfo()) {
            builder.queryParam("needsInfo", "true");
            if (result.getEmail() != null) {
                builder.queryParam("email", result.getEmail());
            }
            if (result.getName() != null) {
                builder.queryParam("name", result.getName());
            }
        }
        return builder.build().toUri();
    }

    @Transactional
    public LoginResponse token(OAuthTokenRequest request) {
        OAuthPending pending = oauthService.consumeAuthCode(request.getAuthCode());
        if (pending.isNewUser()) {
            throw new CustomException(ErrorCode.OAUTH_PENDING_NOT_EXISTING_USER);
        }
        User user = userService.getById(pending.getExistingUserId());
        AuthTokens tokens = authService.issueTokens(user);
        return buildLoginResponse(user, tokens);
    }

    @Transactional
    public LoginResponse complete(OAuthCompleteRequest request) {
        OAuthPending pending = oauthService.consumeAuthCode(request.getAuthCode());
        if (!pending.isNewUser()) {
            throw new CustomException(ErrorCode.OAUTH_PENDING_NOT_NEW_USER);
        }

        String email = resolveEmail(pending, request);
        User user = userService.registerOAuthUser(email, pending.getName(), request.getRole());
        oauthService.link(user, pending.getProvider(), pending.getOauthId());

        switch (request.getRole()) {
            case TEACHER -> createTeacherProfile(user, request.getTeacherInfo());
            case STUDENT -> createStudentProfile(user, request.getStudentInfo());
            case PARENT -> linkParent(user, request.getParentInfo());
        }

        AuthTokens tokens = authService.issueTokens(user);
        return buildLoginResponse(user, tokens);
    }

    private String resolveEmail(OAuthPending pending, OAuthCompleteRequest request) {
        if (pending.getEmail() != null && !pending.getEmail().isBlank()) {
            return pending.getEmail();
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail();
        }
        throw new CustomException(ErrorCode.OAUTH_EMAIL_REQUIRED);
    }

    private void createTeacherProfile(User user, OAuthCompleteRequest.TeacherInfo info) {
        if (info == null) {
            throw new CustomException(ErrorCode.OAUTH_ROLE_INFO_REQUIRED);
        }
        School school = EnumConverter.stringToEnum(info.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        teacherService.createProfile(user, school, info.getGrade(), info.getClassNum());
    }

    private void createStudentProfile(User user, OAuthCompleteRequest.StudentInfo info) {
        if (info == null) {
            throw new CustomException(ErrorCode.OAUTH_ROLE_INFO_REQUIRED);
        }
        School school = EnumConverter.stringToEnum(info.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        studentService.createProfile(user, school, info.getGrade(), info.getClassNum(), info.getNumber());
    }

    private void linkParent(User parent, OAuthCompleteRequest.ParentInfo info) {
        if (info == null) {
            throw new CustomException(ErrorCode.OAUTH_ROLE_INFO_REQUIRED);
        }
        User childUser = userService.getStudentByEmail(info.getChildEmail());
        StudentProfile childProfile = studentService.getProfileByUser(childUser);
        studentService.linkParent(parent, childProfile);
    }

    private LoginResponse buildLoginResponse(User user, AuthTokens tokens) {
        return switch (user.getRole()) {
            case STUDENT -> LoginResponse.ofStudent(user, tokens,
                    studentService.getProfileByUser(user).getId());
            case PARENT -> LoginResponse.ofParent(user, tokens,
                    studentService.getProfilesByParent(user).stream().map(ChildSummary::of).toList());
            default -> LoginResponse.ofTeacher(user, tokens,
                    teacherService.getProfileByUserId(user.getId()));
        };
    }
}
