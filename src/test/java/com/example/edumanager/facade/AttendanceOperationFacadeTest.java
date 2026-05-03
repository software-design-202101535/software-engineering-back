package com.example.edumanager.facade;

import com.example.edumanager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.edumanager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.edumanager.domain.attendance.entity.Attendance;
import com.example.edumanager.domain.attendance.entity.AttendanceStatus;
import com.example.edumanager.domain.attendance.service.AttendanceService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceOperationFacade 단위 테스트")
class AttendanceOperationFacadeTest {

    @Mock AttendanceService attendanceService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;

    @InjectMocks
    AttendanceOperationFacade facade;

    @Mock StudentProfile student;
    @Mock TeacherProfile homeroomTeacher;
    @Mock TeacherProfile nonHomeroomTeacher;
    @Mock Attendance attendance;

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
    @DisplayName("1. getList()")
    class GetList {

        @Test
        @DisplayName("TC-1-1. ADMIN")
        void admin() {
            UserDetailsImpl admin = UserDetailsImpl.create(1L, Role.ADMIN);
            when(studentService.getById(2L)).thenReturn(student);
            when(attendanceService.findByStudentAndMonth(any(), eq(2025), eq(3))).thenReturn(List.of());

            List<?> result = facade.getList(2L, 2025, 3, admin);

            assertAll(
                    () -> verify(attendanceService).findByStudentAndMonth(any(), eq(2025), eq(3)),
                    () -> assertNotNull(result)
            );
        }

        @Test
        @DisplayName("TC-1-2. 담임 TEACHER")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(attendanceService.findByStudentAndMonth(any(), eq(2025), eq(3))).thenReturn(List.of());

            List<?> result = facade.getList(2L, 2025, 3, teacher);

            assertAll(
                    () -> verify(attendanceService).findByStudentAndMonth(any(), eq(2025), eq(3)),
                    () -> assertNotNull(result)
            );
        }

        @Test
        @DisplayName("TC-1-3. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, findByStudentAndMonth never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, 2025, 3, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).findByStudentAndMonth(any(), anyInt(), anyInt())
            );
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-1-4. STUDENT·PARENT → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);
            when(studentService.getById(2L)).thenReturn(student);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, 2025, 3, userDetails));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-1-5. 반 번호 불일치 TEACHER → STUDENT_ACCESS_DENIED, findByStudentAndMonth never")
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
                    () -> facade.getList(2L, 2025, 3, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).findByStudentAndMonth(any(), anyInt(), anyInt())
            );
        }
    }

    @Nested
    @DisplayName("2. create()")
    class Create {

        private final CreateAttendanceRequest request = CreateAttendanceRequest.of(
                LocalDate.of(2025, 3, 10), AttendanceStatus.ABSENT, "감기");

        @Test
        @DisplayName("TC-2-1. 담임 TEACHER → attendanceService.save 호출, 응답 반환")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(attendanceService.save(student, request, homeroomTeacher)).thenReturn(attendance);
            when(attendance.getStudent()).thenReturn(student);
            when(attendance.getDate()).thenReturn(request.getDate());
            when(attendance.getStatus()).thenReturn(request.getStatus());

            var response = facade.create(2L, request, teacher);

            assertAll(
                    () -> verify(attendanceService).save(student, request, homeroomTeacher),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-2-2. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, save never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.create(2L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).save(any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("3. update()")
    class Update {

        private final UpdateAttendanceRequest request = UpdateAttendanceRequest.of(
                LocalDate.of(2025, 3, 11), AttendanceStatus.LATE, "지각");

        @Test
        @DisplayName("TC-3-1. 담임 TEACHER → getByIdAndStudentId → update 호출, 응답 반환")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(attendanceService.getByIdAndStudentId(5L, 2L)).thenReturn(attendance);
            when(attendanceService.update(attendance, request)).thenReturn(attendance);
            when(attendance.getStudent()).thenReturn(student);
            when(attendance.getDate()).thenReturn(request.getDate());
            when(attendance.getStatus()).thenReturn(request.getStatus());

            var response = facade.update(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(attendanceService).getByIdAndStudentId(5L, 2L),
                    () -> verify(attendanceService).update(attendance, request),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-3-2. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, update never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 5L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).update(any(), any())
            );
        }

        @Test
        @DisplayName("TC-3-3. 출결 없음 → ATTENDANCE_NOT_FOUND, update never")
        void attendanceNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(attendanceService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 999L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.ATTENDANCE_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).update(any(), any())
            );
        }
    }

    @Nested
    @DisplayName("4. delete()")
    class Delete {

        @Test
        @DisplayName("TC-4-1. 담임 TEACHER → getByIdAndStudentId → delete 호출")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(attendanceService.getByIdAndStudentId(5L, 2L)).thenReturn(attendance);

            facade.delete(2L, 5L, teacher);

            assertAll(
                    () -> verify(attendanceService).getByIdAndStudentId(5L, 2L),
                    () -> verify(attendanceService).delete(attendance)
            );
        }

        @Test
        @DisplayName("TC-4-2. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, delete never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 5L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).delete(any())
            );
        }

        @Test
        @DisplayName("TC-4-3. 출결 없음 → ATTENDANCE_NOT_FOUND, delete never")
        void attendanceNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(attendanceService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 999L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.ATTENDANCE_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(attendanceService, never()).delete(any())
            );
        }
    }
}
