package com.example.edumanager.facade;

import com.example.edumanager.domain.auth.dto.AuthTokens;
import com.example.edumanager.domain.auth.dto.EmailLoginRequest;
import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.auth.dto.ParentRegisterRequest;
import com.example.edumanager.domain.auth.dto.RefreshResult;
import com.example.edumanager.domain.auth.dto.StudentRegisterRequest;
import com.example.edumanager.domain.auth.dto.TeacherRegisterRequest;
import com.example.edumanager.domain.auth.service.AuthService;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacade 단위 테스트")
class AuthFacadeTest {

    @Mock UserService userService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;
    @Mock AuthService authService;

    @InjectMocks
    AuthFacade facade;

    @Mock User user;
    @Mock User childUser;
    @Mock StudentProfile childProfile;
    @Mock AuthTokens tokens;
    @Mock TeacherProfile teacherProfile;

    @Nested
    @DisplayName("1. registerTeacher()")
    class RegisterTeacher {

        @Test
        @DisplayName("TC-1-1. 성공 → registerSchoolUser → createProfile 순서")
        void success() {
            TeacherRegisterRequest request = TeacherRegisterRequest.of(
                    "teacher@test.com", "pass123!", "pass123!", "홍길동", "SUNRIN_HIGH_SCHOOL", 2, 3);
            when(userService.registerSchoolUser("teacher@test.com", "pass123!", "홍길동", Role.TEACHER))
                    .thenReturn(user);

            facade.registerTeacher(request);

            InOrder inOrder = inOrder(userService, teacherService);
            inOrder.verify(userService).registerSchoolUser("teacher@test.com", "pass123!", "홍길동", Role.TEACHER);
            inOrder.verify(teacherService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3);
        }

        @Test
        @DisplayName("TC-1-2. 비밀번호 불일치 → PASSWORD_MISMATCH, registerSchoolUser never")
        void passwordMismatch() {
            TeacherRegisterRequest request = TeacherRegisterRequest.of(
                    "teacher@test.com", "pass123!", "different!", "홍길동", "SUNRIN_HIGH_SCHOOL", 2, 3);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerTeacher(request));

            assertAll(
                    () -> assertEquals(ErrorCode.PASSWORD_MISMATCH, ex.getErrorCode()),
                    () -> verify(userService, never()).registerSchoolUser(any(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-1-3. 잘못된 학교 → INVALID_SCHOOL, registerSchoolUser never")
        void invalidSchool() {
            TeacherRegisterRequest request = TeacherRegisterRequest.of(
                    "teacher@test.com", "pass123!", "pass123!", "홍길동", "INVALID_SCHOOL", 2, 3);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerTeacher(request));

            assertAll(
                    () -> assertEquals(ErrorCode.INVALID_SCHOOL, ex.getErrorCode()),
                    () -> verify(userService, never()).registerSchoolUser(any(), any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("2. registerStudent()")
    class RegisterStudent {

        @Test
        @DisplayName("TC-2-1. 성공 → registerSchoolUser → createProfile 순서")
        void success() {
            StudentRegisterRequest request = StudentRegisterRequest.of(
                    "student@test.com", "pass123!", "pass123!", "김학생", "SUNRIN_HIGH_SCHOOL", 2, 3, 5);
            when(userService.registerSchoolUser("student@test.com", "pass123!", "김학생", Role.STUDENT))
                    .thenReturn(user);

            facade.registerStudent(request);

            InOrder inOrder = inOrder(userService, studentService);
            inOrder.verify(userService).registerSchoolUser("student@test.com", "pass123!", "김학생", Role.STUDENT);
            inOrder.verify(studentService).createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3, 5);
        }

        @Test
        @DisplayName("TC-2-2. 비밀번호 불일치 → PASSWORD_MISMATCH, registerSchoolUser never")
        void passwordMismatch() {
            StudentRegisterRequest request = StudentRegisterRequest.of(
                    "student@test.com", "pass123!", "different!", "김학생", "SUNRIN_HIGH_SCHOOL", 2, 3, 5);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerStudent(request));

            assertAll(
                    () -> assertEquals(ErrorCode.PASSWORD_MISMATCH, ex.getErrorCode()),
                    () -> verify(userService, never()).registerSchoolUser(any(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-2-3. 잘못된 학교 → INVALID_SCHOOL, registerSchoolUser never")
        void invalidSchool() {
            StudentRegisterRequest request = StudentRegisterRequest.of(
                    "student@test.com", "pass123!", "pass123!", "김학생", "INVALID_SCHOOL", 2, 3, 5);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerStudent(request));

            assertAll(
                    () -> assertEquals(ErrorCode.INVALID_SCHOOL, ex.getErrorCode()),
                    () -> verify(userService, never()).registerSchoolUser(any(), any(), any(), any())
            );
        }

    }

    @Nested
    @DisplayName("3. registerParent()")
    class RegisterParent {

        @Test
        @DisplayName("TC-3-1. 성공 → registerParentUser → getStudentByEmail → getProfileByUser → linkParent 순서")
        void success() {
            ParentRegisterRequest request = ParentRegisterRequest.of(
                    "parent@test.com", "pass123!", "pass123!", "김부모", "student@test.com");
            when(userService.registerParentUser("parent@test.com", "pass123!", "김부모")).thenReturn(user);
            when(userService.getStudentByEmail("student@test.com")).thenReturn(childUser);
            when(studentService.getProfileByUser(childUser)).thenReturn(childProfile);

            facade.registerParent(request);

            InOrder inOrder = inOrder(userService, studentService);
            inOrder.verify(userService).registerParentUser("parent@test.com", "pass123!", "김부모");
            inOrder.verify(userService).getStudentByEmail("student@test.com");
            inOrder.verify(studentService).getProfileByUser(childUser);
            inOrder.verify(studentService).linkParent(user, childProfile);
        }

        @Test
        @DisplayName("TC-3-2. 비밀번호 불일치 → PASSWORD_MISMATCH, registerParentUser never")
        void passwordMismatch() {
            ParentRegisterRequest request = ParentRegisterRequest.of(
                    "parent@test.com", "pass123!", "different!", "김부모", "student@test.com");

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.registerParent(request));

            assertAll(
                    () -> assertEquals(ErrorCode.PASSWORD_MISMATCH, ex.getErrorCode()),
                    () -> verify(userService, never()).registerParentUser(any(), any(), any())
            );
        }

    }

    @Nested
    @DisplayName("4. loginByEmail()")
    class LoginByEmail {

        private EmailLoginRequest stubRequest() {
            return EmailLoginRequest.of("user@test.com", "pass123!");
        }

        private void stubAuth(Role role) {
            when(userService.getByEmail("user@test.com")).thenReturn(user);
            when(user.getRole()).thenReturn(role);
            when(authService.authenticate(user, "pass123!")).thenReturn(tokens);
            when(user.getId()).thenReturn(1L);
            when(user.getEmail()).thenReturn("user@test.com");
            when(user.getName()).thenReturn("홍길동");
            when(tokens.getAccessToken()).thenReturn("access");
            when(tokens.getRefreshToken()).thenReturn("refresh");
        }

        @Test
        @DisplayName("TC-4-1. TEACHER → teacherService.getProfileByUserId 호출, 응답 반환")
        void teacher() {
            stubAuth(Role.TEACHER);
            when(teacherService.getProfileByUserId(1L)).thenReturn(teacherProfile);
            when(teacherProfile.getGrade()).thenReturn(2);
            when(teacherProfile.getClassNum()).thenReturn(3);

            LoginResponse response = facade.loginByEmail(stubRequest());

            verify(teacherService).getProfileByUserId(1L);
            assertAll(
                    () -> assertEquals("access", response.getAccessToken()),
                    () -> assertEquals(1L, response.getUserId()),
                    () -> assertEquals("user@test.com", response.getEmail()),
                    () -> assertEquals("홍길동", response.getName()),
                    () -> assertEquals("TEACHER", response.getRole()),
                    () -> assertEquals(2, response.getGrade()),
                    () -> assertEquals(3, response.getClassNum())
            );
        }

        @Test
        @DisplayName("TC-4-2. STUDENT → studentService.getProfileByUser 호출, 응답 반환")
        void student() {
            stubAuth(Role.STUDENT);
            when(studentService.getProfileByUser(user)).thenReturn(childProfile);
            when(childProfile.getId()).thenReturn(2L);

            LoginResponse response = facade.loginByEmail(stubRequest());

            verify(studentService).getProfileByUser(user);
            assertAll(
                    () -> assertEquals("access", response.getAccessToken()),
                    () -> assertEquals(1L, response.getUserId()),
                    () -> assertEquals("STUDENT", response.getRole()),
                    () -> assertEquals(2L, response.getStudentId())
            );
        }

        @Test
        @DisplayName("TC-4-3. PARENT → studentService.getProfilesByParent 호출, 응답 반환")
        void parent() {
            stubAuth(Role.PARENT);
            when(studentService.getProfilesByParent(user)).thenReturn(List.of());

            LoginResponse response = facade.loginByEmail(stubRequest());

            verify(studentService).getProfilesByParent(user);
            assertAll(
                    () -> assertEquals("access", response.getAccessToken()),
                    () -> assertEquals(1L, response.getUserId()),
                    () -> assertEquals("PARENT", response.getRole())
            );
        }
    }

    @Nested
    @DisplayName("5. refresh()")
    class Refresh {

        @Test
        @DisplayName("TC-5-1. success → authService.refresh 호출, RefreshResult 반환")
        void success() {
            RefreshResult result = RefreshResult.of("newAccess", "newRefresh");
            when(authService.refresh("token")).thenReturn(result);

            assertAll(
                    () -> assertEquals(result, facade.refresh("token")),
                    () -> verify(authService).refresh("token")
            );
        }
    }

    @Nested
    @DisplayName("6. logout()")
    class Logout {

        @Test
        @DisplayName("TC-6-1. success → getById → authService.logout 순서")
        void success() {
            when(userService.getById(1L)).thenReturn(user);

            facade.logout(1L);

            InOrder inOrder = inOrder(userService, authService);
            inOrder.verify(userService).getById(1L);
            inOrder.verify(authService).logout(user);
        }
    }
}
