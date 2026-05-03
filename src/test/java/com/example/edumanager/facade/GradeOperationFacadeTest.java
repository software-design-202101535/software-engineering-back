package com.example.edumanager.facade;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.grade.service.GradeService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeOperationFacade 단위 테스트")
class GradeOperationFacadeTest {

    @Mock GradeService gradeService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;

    @InjectMocks
    GradeOperationFacade facade;

    @Mock StudentProfile student;
    @Mock User studentUser;
    @Mock TeacherProfile homeroomTeacher;
    @Mock TeacherProfile nonHomeroomTeacher;

    private void stubStudent() {
        when(student.getGrade()).thenReturn(2);
        when(student.getClassNum()).thenReturn(3);
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
    @DisplayName("1. getGrades() - 성공")
    class GetGradesSuccess {

        @Test
        @DisplayName("TC-1-1. ADMIN")
        void admin() {
            UserDetailsImpl admin = UserDetailsImpl.create(1L, Role.ADMIN);
            when(studentService.getById(2L)).thenReturn(student);
            when(gradeService.getGrades(student, "2025-1", ExamType.MIDTERM)).thenReturn(List.of());

            facade.getGrades(2L, admin, "2025-1", ExamType.MIDTERM);

            verify(gradeService).getGrades(student, "2025-1", ExamType.MIDTERM);
        }

        @Test
        @DisplayName("TC-1-2. 담임 교사")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(gradeService.getGrades(student, "2025-1", ExamType.MIDTERM)).thenReturn(List.of());

            facade.getGrades(2L, teacher, "2025-1", ExamType.MIDTERM);

            verify(gradeService).getGrades(student, "2025-1", ExamType.MIDTERM);
        }

        @Test
        @DisplayName("TC-1-3. 본인 학생")
        void ownStudent() {
            UserDetailsImpl studentDetails = UserDetailsImpl.create(10L, Role.STUDENT);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getId()).thenReturn(10L);
            when(gradeService.getGrades(student, "2025-1", ExamType.MIDTERM)).thenReturn(List.of());

            facade.getGrades(2L, studentDetails, "2025-1", ExamType.MIDTERM);

            verify(gradeService).getGrades(student, "2025-1", ExamType.MIDTERM);
        }

        @Test
        @DisplayName("TC-1-4. 연결된 학부모")
        void linkedParent() {
            UserDetailsImpl parentDetails = UserDetailsImpl.create(20L, Role.PARENT);
            User linkedParent = mock(User.class);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getId()).thenReturn(2L);
            when(studentService.getParentsByStudentId(2L)).thenReturn(List.of(linkedParent));
            when(linkedParent.getId()).thenReturn(20L);
            when(gradeService.getGrades(student, "2025-1", ExamType.MIDTERM)).thenReturn(List.of());

            facade.getGrades(2L, parentDetails, "2025-1", ExamType.MIDTERM);

            verify(gradeService).getGrades(student, "2025-1", ExamType.MIDTERM);
        }
    }

    @Nested
    @DisplayName("2. getGrades() - 실패")
    class GetGradesFail {

        @Test
        @DisplayName("TC-2-1. 담임 아닌 교사 → GRADE_ACCESS_DENIED")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getGrades(2L, teacher, "2025-1", ExamType.MIDTERM));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(gradeService, never()).getGrades(any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-2-2. 타인 학생 → GRADE_ACCESS_DENIED")
        void otherStudent() {
            UserDetailsImpl studentDetails = UserDetailsImpl.create(99L, Role.STUDENT);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getId()).thenReturn(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getGrades(2L, studentDetails, "2025-1", ExamType.MIDTERM));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(gradeService, never()).getGrades(any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-2-3. 미연결 학부모 → GRADE_ACCESS_DENIED")
        void unlinkedParent() {
            UserDetailsImpl parentDetails = UserDetailsImpl.create(20L, Role.PARENT);
            when(studentService.getById(2L)).thenReturn(student);
            when(student.getId()).thenReturn(2L);
            when(studentService.getParentsByStudentId(2L)).thenReturn(List.of());

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getGrades(2L, parentDetails, "2025-1", ExamType.MIDTERM));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(gradeService, never()).getGrades(any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-2-4. 존재하지 않는 학생 → STUDENT_NOT_FOUND")
        void studentNotFound() {
            UserDetailsImpl admin = UserDetailsImpl.create(1L, Role.ADMIN);
            when(studentService.getById(999L)).thenThrow(new CustomException(ErrorCode.STUDENT_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getGrades(999L, admin, "2025-1", ExamType.MIDTERM));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(gradeService, never()).getGrades(any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-2-5. 반 번호 불일치 TEACHER → GRADE_ACCESS_DENIED")
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
                    () -> facade.getGrades(2L, teacher, "2025-1", ExamType.MIDTERM));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(gradeService, never()).getGrades(any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("3. batchProcess() - 성공")
    class BatchProcessSuccess {

        private final BatchGradeRequest request = BatchGradeRequest.of(
                "2025-1", ExamType.MIDTERM, List.of(), List.of(), List.of());

        @Test
        @DisplayName("TC-3-1. ADMIN")
        void admin() {
            UserDetailsImpl admin = UserDetailsImpl.create(1L, Role.ADMIN);
            when(studentService.getById(2L)).thenReturn(student);
            when(gradeService.batchProcess(student, request)).thenReturn(List.of());

            facade.batchProcess(2L, admin, request);

            verify(gradeService).batchProcess(student, request);
        }

        @Test
        @DisplayName("TC-3-2. 담임 교사")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(gradeService.batchProcess(student, request)).thenReturn(List.of());

            facade.batchProcess(2L, teacher, request);

            verify(gradeService).batchProcess(student, request);
        }
    }

    @Nested
    @DisplayName("4. batchProcess() - 실패")
    class BatchProcessFail {

        private final BatchGradeRequest request = BatchGradeRequest.of(
                "2025-1", ExamType.MIDTERM, List.of(), List.of(), List.of());

        @Test
        @DisplayName("TC-4-1. 담임 아닌 교사 → GRADE_ACCESS_DENIED")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.batchProcess(2L, teacher, request));

            assertAll(
                    () -> assertEquals(ErrorCode.GRADE_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(gradeService, never()).batchProcess(any(), any())
            );
        }

        @Test
        @DisplayName("TC-4-2. 존재하지 않는 학생 → STUDENT_NOT_FOUND")
        void studentNotFound() {
            UserDetailsImpl admin = UserDetailsImpl.create(1L, Role.ADMIN);
            when(studentService.getById(999L)).thenThrow(new CustomException(ErrorCode.STUDENT_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.batchProcess(999L, admin, request));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(gradeService, never()).batchProcess(any(), any())
            );
        }
    }
}
