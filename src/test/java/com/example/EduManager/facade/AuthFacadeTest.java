package com.example.EduManager.facade;

import com.example.EduManager.domain.auth.dto.ParentRegisterRequest;
import com.example.EduManager.domain.auth.dto.StudentRegisterRequest;
import com.example.EduManager.domain.auth.dto.TeacherRegisterRequest;
import com.example.EduManager.domain.auth.service.AuthService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.service.UserService;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
