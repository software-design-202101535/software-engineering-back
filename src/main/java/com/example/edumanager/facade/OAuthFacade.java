package com.example.edumanager.facade;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.ChildSummary;
import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.auth.service.AuthService;
import com.example.edumanager.domain.oauth.dto.OAuthLoginRequest;
import com.example.edumanager.domain.oauth.dto.OAuthLoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthRegisterRequest;
import com.example.edumanager.domain.oauth.service.OAuthLoginResult;
import com.example.edumanager.domain.oauth.service.OAuthService;
import com.example.edumanager.domain.oauth.service.OAuthTempTokenPayload;
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

@Component
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthService oauthService;
    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AuthService authService;

    @Transactional
    public OAuthLoginResponse loginWithKakao(OAuthLoginRequest request) {
        OAuthLoginResult result = oauthService.loginWithKakao(request.getCode());

        if (result.isNewUser()) {
            return OAuthLoginResponse.newUser(result.getTempToken(), result.getEmail(), result.getName());
        }

        User user = userService.getById(result.getExistingUserId());
        AuthTokens tokens = authService.issueTokens(user);
        return OAuthLoginResponse.existing(buildLoginResponse(user, tokens));
    }

    @Transactional
    public LoginResponse registerWithKakao(OAuthRegisterRequest request) {
        OAuthTempTokenPayload payload = oauthService.parseTempToken(request.getTempToken());

        String email = resolveEmail(payload, request);
        User user = userService.registerOAuthUser(email, payload.getName(), request.getRole());
        oauthService.link(user, payload.getProvider(), payload.getOauthId());

        switch (request.getRole()) {
            case TEACHER -> createTeacherProfile(user, request.getTeacherInfo());
            case STUDENT -> createStudentProfile(user, request.getStudentInfo());
            case PARENT -> linkParent(user, request.getParentInfo());
        }

        AuthTokens tokens = authService.issueTokens(user);
        return buildLoginResponse(user, tokens);
    }

    private String resolveEmail(OAuthTempTokenPayload payload, OAuthRegisterRequest request) {
        if (payload.getEmail() != null && !payload.getEmail().isBlank()) {
            return payload.getEmail();
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return request.getEmail();
        }
        throw new CustomException(ErrorCode.OAUTH_EMAIL_REQUIRED);
    }

    private void createTeacherProfile(User user, OAuthRegisterRequest.TeacherInfo info) {
        if (info == null) {
            throw new CustomException(ErrorCode.OAUTH_ROLE_INFO_REQUIRED);
        }
        School school = EnumConverter.stringToEnum(info.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        teacherService.createProfile(user, school, info.getGrade(), info.getClassNum());
    }

    private void createStudentProfile(User user, OAuthRegisterRequest.StudentInfo info) {
        if (info == null) {
            throw new CustomException(ErrorCode.OAUTH_ROLE_INFO_REQUIRED);
        }
        School school = EnumConverter.stringToEnum(info.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        studentService.createProfile(user, school, info.getGrade(), info.getClassNum(), info.getNumber());
    }

    private void linkParent(User parent, OAuthRegisterRequest.ParentInfo info) {
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
