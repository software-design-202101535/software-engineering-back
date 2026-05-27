package com.example.edumanager.facade;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.auth.service.AuthService;
import com.example.edumanager.domain.oauth.dto.OAuthLoginRequest;
import com.example.edumanager.domain.oauth.dto.OAuthLoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthRegisterRequest;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.oauth.service.OAuthLoginResult;
import com.example.edumanager.domain.oauth.service.OAuthService;
import com.example.edumanager.domain.oauth.service.OAuthTempTokenPayload;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthFacade 단위 테스트")
class OAuthFacadeTest {

    @Mock OAuthService oauthService;
    @Mock UserService userService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;
    @Mock AuthService authService;

    @InjectMocks OAuthFacade facade;

    @Mock User user;
    @Mock User childUser;
    @Mock StudentProfile childProfile;
    @Mock StudentProfile myProfile;
    @Mock TeacherProfile teacherProfile;
    @Mock AuthTokens tokens;

    @Nested
    @DisplayName("1. loginWithKakao()")
    class LoginWithKakao {

        @Test
        @DisplayName("TC-1-1. 기존 유저(TEACHER) → existing(LoginResponse) 반환")
        void existingUser() {
            OAuthLoginRequest request = OAuthLoginRequest.of("code-1");
            when(oauthService.loginWithKakao("code-1")).thenReturn(OAuthLoginResult.existing(7L));
            when(userService.getById(7L)).thenReturn(user);
            when(user.getId()).thenReturn(7L);
            when(user.getEmail()).thenReturn("t@k.com");
            when(user.getName()).thenReturn("교사");
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(tokens.getAccessToken()).thenReturn("access-x");
            when(tokens.getRefreshToken()).thenReturn("refresh-x");
            when(teacherService.getProfileByUserId(7L)).thenReturn(teacherProfile);

            OAuthLoginResponse response = facade.loginWithKakao(request);
            LoginResponse loginData = response.getLoginData();

            assertAll(
                    () -> assertFalse(response.isNewUser()),
                    () -> assertEquals(7L, loginData.getUserId()),
                    () -> assertEquals("t@k.com", loginData.getEmail()),
                    () -> assertEquals("교사", loginData.getName()),
                    () -> assertEquals("TEACHER", loginData.getRole()),
                    () -> assertEquals("access-x", loginData.getAccessToken()),
                    () -> assertEquals("refresh-x", loginData.getRefreshToken())
            );
        }

        @Test
        @DisplayName("TC-1-2. 신규 유저 → newUser(tempToken) 반환, 후속 호출 never")
        void newUser() {
            OAuthLoginRequest request = OAuthLoginRequest.of("code-2");
            when(oauthService.loginWithKakao("code-2"))
                    .thenReturn(OAuthLoginResult.newUser("temp-jwt"));

            OAuthLoginResponse response = facade.loginWithKakao(request);

            assertAll(
                    () -> assertTrue(response.isNewUser()),
                    () -> assertEquals("temp-jwt", response.getTempToken()),
                    () -> verify(userService, never()).getById(any()),
                    () -> verify(authService, never()).issueTokens(any())
            );
        }
    }

    @Nested
    @DisplayName("2. registerWithKakao()")
    class RegisterWithKakao {

        @Test
        @DisplayName("TC-2-1. TEACHER 정상 → InOrder: parseTempToken → registerOAuthUser → link → createProfile → issueTokens")
        void teacherSuccess() {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", Role.TEACHER, "t@k.com", "교사",
                    OAuthRegisterRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 2, 3),
                    null, null);
            OAuthTempTokenPayload payload = OAuthTempTokenPayload.of("kakao-1", OAuthProvider.KAKAO);

            when(oauthService.parseTempToken("temp-jwt")).thenReturn(payload);
            when(userService.registerOAuthUser("t@k.com", "교사", Role.TEACHER)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(teacherService.getProfileByUserId(any())).thenReturn(teacherProfile);

            facade.registerWithKakao(request);

            InOrder order = inOrder(oauthService, userService, teacherService, authService);
            order.verify(oauthService).parseTempToken("temp-jwt");
            order.verify(userService).registerOAuthUser("t@k.com", "교사", Role.TEACHER);
            order.verify(oauthService).link(user, OAuthProvider.KAKAO, "kakao-1");
            order.verify(teacherService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3);
            order.verify(authService).issueTokens(user);
        }

        @Test
        @DisplayName("TC-2-2. STUDENT 정상 → studentService.createProfile 호출")
        void studentSuccess() {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", Role.STUDENT, "s@k.com", "학생", null,
                    OAuthRegisterRequest.StudentInfo.of("SUNRIN_HIGH_SCHOOL", 1, 2, 15),
                    null);
            OAuthTempTokenPayload payload = OAuthTempTokenPayload.of("kakao-1", OAuthProvider.KAKAO);

            when(oauthService.parseTempToken("temp-jwt")).thenReturn(payload);
            when(userService.registerOAuthUser("s@k.com", "학생", Role.STUDENT)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.STUDENT);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(studentService.getProfileByUser(user)).thenReturn(myProfile);
            when(myProfile.getId()).thenReturn(11L);

            facade.registerWithKakao(request);

            verify(studentService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 1, 2, 15);
        }

        @Test
        @DisplayName("TC-2-3. PARENT 정상 → getStudentByEmail + linkParent 호출")
        void parentSuccess() {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", Role.PARENT, "p@k.com", "학부모", null, null,
                    OAuthRegisterRequest.ParentInfo.of("child@k.com"));
            OAuthTempTokenPayload payload = OAuthTempTokenPayload.of("kakao-1", OAuthProvider.KAKAO);

            when(oauthService.parseTempToken("temp-jwt")).thenReturn(payload);
            when(userService.registerOAuthUser("p@k.com", "학부모", Role.PARENT)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.PARENT);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(userService.getStudentByEmail("child@k.com")).thenReturn(childUser);
            when(studentService.getProfileByUser(childUser)).thenReturn(childProfile);
            when(studentService.getProfilesByParent(user)).thenReturn(List.of());

            facade.registerWithKakao(request);

            verify(userService).getStudentByEmail("child@k.com");
            verify(studentService).linkParent(user, childProfile);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"TEACHER", "STUDENT", "PARENT"})
        @DisplayName("TC-2-4. role-info 누락 → OAUTH_ROLE_INFO_REQUIRED")
        void roleInfoMissing(Role role) {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", role, "e@k.com", "n", null, null, null);
            OAuthTempTokenPayload payload = OAuthTempTokenPayload.of("kakao-1", OAuthProvider.KAKAO);
            when(oauthService.parseTempToken("temp-jwt")).thenReturn(payload);
            when(userService.registerOAuthUser(any(), any(), any())).thenReturn(user);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerWithKakao(request));

            assertEquals(ErrorCode.OAUTH_ROLE_INFO_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-2-5. registerOAuthUser 중복 throw → link/createProfile/issueTokens never (partial-failure 가드)")
        void registerDuplicatedRollback() {
            OAuthRegisterRequest request = OAuthRegisterRequest.of(
                    "temp-jwt", Role.TEACHER, "dup@k.com", "n",
                    OAuthRegisterRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1), null, null);
            OAuthTempTokenPayload payload = OAuthTempTokenPayload.of("kakao-1", OAuthProvider.KAKAO);

            when(oauthService.parseTempToken("temp-jwt")).thenReturn(payload);
            when(userService.registerOAuthUser("dup@k.com", "n", Role.TEACHER))
                    .thenThrow(new CustomException(ErrorCode.DUPLICATED_USER));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerWithKakao(request));

            assertAll(
                    () -> assertEquals(ErrorCode.DUPLICATED_USER, ex.getErrorCode()),
                    () -> verify(oauthService, never()).link(any(), any(), any()),
                    () -> verify(teacherService, never()).createProfile(any(), any(), anyInt(), anyInt()),
                    () -> verify(authService, never()).issueTokens(any())
            );
        }
    }
}
