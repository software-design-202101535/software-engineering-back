package com.example.edumanager.facade;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.auth.service.AuthService;
import com.example.edumanager.domain.oauth.client.OAuthProperties;
import com.example.edumanager.domain.oauth.dto.OAuthAuthorizeResult;
import com.example.edumanager.domain.oauth.dto.OAuthCompleteRequest;
import com.example.edumanager.domain.oauth.dto.OAuthTokenRequest;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import com.example.edumanager.domain.oauth.service.OAuthPending;
import com.example.edumanager.domain.oauth.service.OAuthService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock User user;
    @Mock User childUser;
    @Mock StudentProfile childProfile;
    @Mock StudentProfile myProfile;
    @Mock TeacherProfile teacherProfile;
    @Mock AuthTokens tokens;

    OAuthFacade facade;

    private static final OAuthProperties PROPERTIES = new OAuthProperties(
            new OAuthProperties.Kakao(
                    "test-client",
                    null,
                    "http://localhost:8080/api/auth/oauth/kakao/callback",
                    "http://localhost:5173/oauth/result",
                    "https://kauth.kakao.com/oauth/authorize",
                    "https://kauth.kakao.com/oauth/token",
                    "https://kapi.kakao.com/v2/user/me"
            )
    );

    @BeforeEach
    void setUp() {
        facade = new OAuthFacade(oauthService, userService, studentService, teacherService, authService, PROPERTIES);
    }

    @Nested
    @DisplayName("1. buildKakaoAuthorizeUrl()")
    class BuildKakaoAuthorizeUrl {

        @Test
        @DisplayName("TC-1-1. URL에 client_id/redirect_uri/response_type=code 포함")
        void success() {
            URI uri = facade.buildKakaoAuthorizeUrl();
            String url = uri.toString();

            assertAll(
                    () -> assertTrue(url.startsWith("https://kauth.kakao.com/oauth/authorize")),
                    () -> assertTrue(url.contains("client_id=test-client")),
                    () -> assertTrue(url.contains("response_type=code")),
                    () -> assertTrue(url.contains("redirect_uri="))
            );
        }
    }

    @Nested
    @DisplayName("2. buildFrontendRedirectUrl()")
    class BuildFrontendRedirectUrl {

        @Test
        @DisplayName("TC-2-1. 기존 유저 → authCode 만 포함, needsInfo 없음")
        void existingUser() {
            when(oauthService.handleKakaoCallback("code"))
                    .thenReturn(OAuthAuthorizeResult.of("auth-1", false, "e@k.com", "name"));

            URI uri = facade.buildFrontendRedirectUrl("code");
            String url = uri.toString();

            assertAll(
                    () -> assertTrue(url.startsWith("http://localhost:5173/oauth/result")),
                    () -> assertTrue(url.contains("authCode=auth-1")),
                    () -> assertTrue(!url.contains("needsInfo"))
            );
        }

        @Test
        @DisplayName("TC-2-2. 신규 유저 + email/name 있음 → needsInfo=true, email, name 포함")
        void newUserWithEmailAndName() {
            when(oauthService.handleKakaoCallback("code"))
                    .thenReturn(OAuthAuthorizeResult.of("auth-2", true, "new@k.com", "신규"));

            String url = facade.buildFrontendRedirectUrl("code").toString();

            assertAll(
                    () -> assertTrue(url.contains("authCode=auth-2")),
                    () -> assertTrue(url.contains("needsInfo=true")),
                    () -> assertTrue(url.contains("email=new@k.com")),
                    () -> assertTrue(url.contains("name=") && (url.contains("신규") || url.contains("%EC%8B%A0%EA%B7%9C")))
            );
        }

        @Test
        @DisplayName("TC-2-3. 신규 유저 + email/name 없음 → needsInfo=true 만 포함")
        void newUserNoEmailAndName() {
            when(oauthService.handleKakaoCallback("code"))
                    .thenReturn(OAuthAuthorizeResult.of("auth-3", true, null, null));

            String url = facade.buildFrontendRedirectUrl("code").toString();

            assertAll(
                    () -> assertTrue(url.contains("authCode=auth-3")),
                    () -> assertTrue(url.contains("needsInfo=true")),
                    () -> assertTrue(!url.contains("email=")),
                    () -> assertTrue(!url.contains("name="))
            );
        }
    }

    @Nested
    @DisplayName("3. token()")
    class Token {

        @Test
        @DisplayName("TC-3-1. 기존 유저(TEACHER) → consume → getById → issueTokens 순서, LoginResponse 반환")
        void success() {
            OAuthTokenRequest request = OAuthTokenRequest.of("auth-1");
            OAuthPending pending = OAuthPending.forExistingUser(OAuthProvider.KAKAO, "kakao-1", 7L, "e@k.com", "name");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.getById(7L)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(teacherService.getProfileByUserId(any())).thenReturn(teacherProfile);

            LoginResponse response = facade.token(request);

            InOrder order = inOrder(oauthService, userService, authService);
            order.verify(oauthService).consumeAuthCode("auth-1");
            order.verify(userService).getById(7L);
            order.verify(authService).issueTokens(user);

            assertEquals(LoginResponse.ofTeacher(user, tokens, teacherProfile).getUserId(), response.getUserId());
        }

        @Test
        @DisplayName("TC-3-2. 신규 유저 → OAUTH_PENDING_NOT_EXISTING_USER, 후속 호출 never")
        void newUser() {
            OAuthTokenRequest request = OAuthTokenRequest.of("auth-1");
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "e@k.com", "name");
            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);

            CustomException ex = assertThrows(CustomException.class, () -> facade.token(request));

            assertAll(
                    () -> assertEquals(ErrorCode.OAUTH_PENDING_NOT_EXISTING_USER, ex.getErrorCode()),
                    () -> verify(userService, never()).getById(any()),
                    () -> verify(authService, never()).issueTokens(any())
            );
        }
    }

    @Nested
    @DisplayName("4. complete()")
    class Complete {

        @Test
        @DisplayName("TC-4-1. TEACHER 정상 → InOrder: registerOAuthUser → link → createProfile → issueTokens")
        void teacherSuccess() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, null,
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 2, 3),
                    null, null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "t@k.com", "교사");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser("t@k.com", "교사", Role.TEACHER)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(teacherService.getProfileByUserId(any())).thenReturn(teacherProfile);

            facade.complete(request);

            InOrder order = inOrder(oauthService, userService, teacherService, authService);
            order.verify(oauthService).consumeAuthCode("auth-1");
            order.verify(userService).registerOAuthUser("t@k.com", "교사", Role.TEACHER);
            order.verify(oauthService).link(user, OAuthProvider.KAKAO, "kakao-1");
            order.verify(teacherService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3);
            order.verify(authService).issueTokens(user);
        }

        @Test
        @DisplayName("TC-4-2. STUDENT 정상 → studentService.createProfile 호출")
        void studentSuccess() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.STUDENT, null, null,
                    OAuthCompleteRequest.StudentInfo.of("SUNRIN_HIGH_SCHOOL", 1, 2, 15),
                    null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "s@k.com", "학생");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser("s@k.com", "학생", Role.STUDENT)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.STUDENT);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(studentService.getProfileByUser(user)).thenReturn(myProfile);
            when(myProfile.getId()).thenReturn(11L);

            facade.complete(request);

            verify(studentService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 1, 2, 15);
        }

        @Test
        @DisplayName("TC-4-3. PARENT 정상 → getStudentByEmail + linkParent 호출")
        void parentSuccess() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.PARENT, null, null, null,
                    OAuthCompleteRequest.ParentInfo.of("child@k.com"));
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "p@k.com", "학부모");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser("p@k.com", "학부모", Role.PARENT)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.PARENT);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(userService.getStudentByEmail("child@k.com")).thenReturn(childUser);
            when(studentService.getProfileByUser(childUser)).thenReturn(childProfile);
            when(studentService.getProfilesByParent(user)).thenReturn(List.of());

            facade.complete(request);

            verify(userService).getStudentByEmail("child@k.com");
            verify(studentService).linkParent(user, childProfile);
        }

        @Test
        @DisplayName("TC-4-4. 기존 유저 → OAUTH_PENDING_NOT_NEW_USER, 후속 호출 never")
        void existingUser() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, null,
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1), null, null);
            OAuthPending pending = OAuthPending.forExistingUser(OAuthProvider.KAKAO, "kakao-1", 7L, "e", "n");
            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);

            CustomException ex = assertThrows(CustomException.class, () -> facade.complete(request));

            assertAll(
                    () -> assertEquals(ErrorCode.OAUTH_PENDING_NOT_NEW_USER, ex.getErrorCode()),
                    () -> verify(userService, never()).registerOAuthUser(any(), any(), any())
            );
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"TEACHER", "STUDENT", "PARENT"})
        @DisplayName("TC-4-5. role-info 누락 → OAUTH_ROLE_INFO_REQUIRED")
        void roleInfoMissing(Role role) {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", role, null, null, null, null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "e@k.com", "n");
            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser(any(), any(), any())).thenReturn(user);

            CustomException ex = assertThrows(CustomException.class, () -> facade.complete(request));

            assertEquals(ErrorCode.OAUTH_ROLE_INFO_REQUIRED, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-4-6. pending.email + request.email 둘 다 없음 → OAUTH_EMAIL_REQUIRED")
        void emailMissing() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, null,
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1), null, null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", null, "n");
            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);

            CustomException ex = assertThrows(CustomException.class, () -> facade.complete(request));

            assertAll(
                    () -> assertEquals(ErrorCode.OAUTH_EMAIL_REQUIRED, ex.getErrorCode()),
                    () -> verify(userService, never()).registerOAuthUser(any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-4-7. pending.email 있음 → pending 값 사용")
        void emailFromPending() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, "request@k.com",
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1), null, null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", "pending@k.com", "n");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser("pending@k.com", "n", Role.TEACHER)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(teacherService.getProfileByUserId(any())).thenReturn(teacherProfile);

            facade.complete(request);

            verify(userService).registerOAuthUser("pending@k.com", "n", Role.TEACHER);
        }

        @Test
        @DisplayName("TC-4-8. pending.email 없고 request.email 있음 → request 값 사용")
        void emailFromRequest() {
            OAuthCompleteRequest request = OAuthCompleteRequest.of(
                    "auth-1", Role.TEACHER, "request@k.com",
                    OAuthCompleteRequest.TeacherInfo.of("SUNRIN_HIGH_SCHOOL", 1, 1), null, null);
            OAuthPending pending = OAuthPending.forNewUser(OAuthProvider.KAKAO, "kakao-1", null, "n");

            when(oauthService.consumeAuthCode("auth-1")).thenReturn(pending);
            when(userService.registerOAuthUser("request@k.com", "n", Role.TEACHER)).thenReturn(user);
            when(user.getRole()).thenReturn(Role.TEACHER);
            when(authService.issueTokens(user)).thenReturn(tokens);
            when(teacherService.getProfileByUserId(any())).thenReturn(teacherProfile);

            facade.complete(request);

            verify(userService).registerOAuthUser("request@k.com", "n", Role.TEACHER);
        }
    }
}
