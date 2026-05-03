package com.example.edumanager.facade;

import com.example.edumanager.domain.student.dto.UpdateStudentRequest;
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
import com.example.edumanager.global.security.UserDetailsImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentOperationFacade 단위 테스트")
class StudentOperationFacadeTest {

    @Mock StudentService studentService;
    @Mock TeacherService teacherService;
    @Mock UserService userService;

    @InjectMocks
    StudentOperationFacade facade;

    @Mock StudentProfile student;
    @Mock User studentUser;
    @Mock TeacherProfile homeroomTeacher;
    @Mock TeacherProfile nonHomeroomTeacher;

    private void stubStudent() {
        when(student.getGrade()).thenReturn(2);
        when(student.getClassNum()).thenReturn(3);
        when(student.getUser()).thenReturn(studentUser);
        when(student.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
    }

    private void stubHomeroomTeacher(Long teacherUserId) {
        when(teacherService.getProfileByUserId(teacherUserId)).thenReturn(homeroomTeacher);
        when(homeroomTeacher.getGrade()).thenReturn(2);
        when(homeroomTeacher.getClassNum()).thenReturn(3);
        when(homeroomTeacher.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
    }

    private void stubNonHomeroomTeacher(Long teacherUserId) {
        when(teacherService.getProfileByUserId(teacherUserId)).thenReturn(nonHomeroomTeacher);
        when(nonHomeroomTeacher.getGrade()).thenReturn(1);
        when(student.getGrade()).thenReturn(2);
    }

    @Nested
    @DisplayName("1. getClassStudents() - 성공")
    class GetClassStudentsSuccess {

        @Test
        @DisplayName("TC-1-2. TEACHER → 반 학생 목록 반환")
        void teacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(teacherService.getProfileByUserId(10L)).thenReturn(homeroomTeacher);
            when(homeroomTeacher.getGrade()).thenReturn(2);
            when(homeroomTeacher.getClassNum()).thenReturn(3);
            when(homeroomTeacher.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
            when(studentService.getClassStudents(2, 3, School.SUNRIN_HIGH_SCHOOL)).thenReturn(List.of());

            List<?> result = facade.getClassStudents(teacher);

            assertAll(
                    () -> verify(teacherService).getProfileByUserId(10L),
                    () -> verify(studentService).getClassStudents(2, 3, School.SUNRIN_HIGH_SCHOOL),
                    () -> assertNotNull(result)
            );
        }
    }

    @Nested
    @DisplayName("2. getClassStudents() - 실패")
    class GetClassStudentsFail {

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-1-1. TEACHER 아닌 역할 → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getClassStudents(userDetails));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(teacherService, never()).getProfileByUserId(any()),
                    () -> verify(studentService, never()).getClassStudents(anyInt(), anyInt(), any())
            );
        }
    }

    @Nested
    @DisplayName("3. getStudentDetail() - 성공")
    class GetStudentDetailSuccess {

        @Test
        @DisplayName("TC-2-1. 담임 TEACHER")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(studentUser.getName()).thenReturn("홍길동");

            var response = facade.getStudentDetail(2L, teacher);

            assertAll(
                    () -> verify(teacherService).getProfileByUserId(10L),
                    () -> assertEquals("홍길동", response.getName())
            );
        }
    }

    @Nested
    @DisplayName("4. getStudentDetail() - 실패")
    class GetStudentDetailFail {

        @Test
        @DisplayName("TC-2-3. 담임 아닌 TEACHER (학년 불일치) → STUDENT_ACCESS_DENIED")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getStudentDetail(2L, teacher));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-2-3-1. 반 번호 불일치 TEACHER → STUDENT_ACCESS_DENIED")
        void classMismatchTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getGrade()).thenReturn(2);
            when(student.getClassNum()).thenReturn(3);
            TeacherProfile classMismatch = mock(TeacherProfile.class);
            when(teacherService.getProfileByUserId(10L)).thenReturn(classMismatch);
            when(classMismatch.getGrade()).thenReturn(2);
            when(classMismatch.getClassNum()).thenReturn(4);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getStudentDetail(2L, teacher));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-2-3-2. 학교 불일치 TEACHER → STUDENT_ACCESS_DENIED")
        void schoolMismatchTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getGrade()).thenReturn(2);
            when(student.getClassNum()).thenReturn(3);
            when(student.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
            TeacherProfile schoolMismatch = mock(TeacherProfile.class);
            when(teacherService.getProfileByUserId(10L)).thenReturn(schoolMismatch);
            when(schoolMismatch.getGrade()).thenReturn(2);
            when(schoolMismatch.getClassNum()).thenReturn(3);
            when(schoolMismatch.getSchool()).thenReturn(School.SEOUL_HIGH_SCHOOL);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getStudentDetail(2L, teacher));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-2-4. STUDENT·PARENT → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);
            when(studentService.getById(2L)).thenReturn(student);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getStudentDetail(2L, userDetails));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("5. updateStudentDetail() - 성공")
    class UpdateStudentDetailSuccess {

        @Test
        @DisplayName("TC-3-1. 담임 TEACHER → userService.updateName → studentService.updateDetail 순서")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            UpdateStudentRequest request = UpdateStudentRequest.of("김철수",
                    "2008-01-01", "010-1234-5678", "010-9876-5432", "서울시");
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(studentUser.getName()).thenReturn("김철수");

            facade.updateStudentDetail(2L, request, teacher);

            InOrder inOrder = inOrder(userService, studentService);
            inOrder.verify(userService).updateName(student.getUser(), request.getName());
            inOrder.verify(studentService).updateDetail(student, request);
        }
    }

    @Nested
    @DisplayName("6. updateStudentDetail() - 실패")
    class UpdateStudentDetailFail {

        @Test
        @DisplayName("TC-3-2. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, updateName·updateDetail never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            UpdateStudentRequest request = UpdateStudentRequest.of("김철수",
                    "2008-01-01", "010-1234-5678", "010-9876-5432", "서울시");
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.updateStudentDetail(2L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(userService, never()).updateName(any(), any()),
                    () -> verify(studentService, never()).updateDetail(any(), any())
            );
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-3-3. STUDENT·PARENT → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);
            UpdateStudentRequest request = UpdateStudentRequest.of("김철수",
                    "2008-01-01", "010-1234-5678", "010-9876-5432", "서울시");
            when(studentService.getById(2L)).thenReturn(student);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.updateStudentDetail(2L, request, userDetails));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }
    }
}
