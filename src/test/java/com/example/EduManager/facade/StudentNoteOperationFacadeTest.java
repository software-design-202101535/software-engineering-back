package com.example.EduManager.facade;

import com.example.EduManager.domain.student.dto.CreateNoteRequest;
import com.example.EduManager.domain.student.dto.UpdateNoteRequest;
import com.example.EduManager.domain.student.entity.NoteCategory;
import com.example.EduManager.domain.student.entity.StudentNote;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentNoteService;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.service.UserService;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.UserDetailsImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentNoteOperationFacade 단위 테스트")
class StudentNoteOperationFacadeTest {

    @Mock StudentNoteService studentNoteService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;
    @Mock UserService userService;

    @InjectMocks
    StudentNoteOperationFacade facade;

    @Mock StudentProfile student;
    @Mock User studentUser;
    @Mock TeacherProfile homeroomTeacher;
    @Mock TeacherProfile nonHomeroomTeacher;
    @Mock User homeroomTeacherUser;
    @Mock StudentNote note;

    private void stubStudent() {
        when(student.getGrade()).thenReturn(2);
        when(student.getClassNum()).thenReturn(3);
        when(student.getUser()).thenReturn(studentUser);
        when(studentUser.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
    }

    private void stubHomeroomTeacher(Long teacherUserId) {
        when(teacherService.getProfileByUserId(teacherUserId)).thenReturn(homeroomTeacher);
        when(homeroomTeacher.getGrade()).thenReturn(2);
        when(homeroomTeacher.getClassNum()).thenReturn(3);
        when(homeroomTeacher.getUser()).thenReturn(homeroomTeacherUser);
        when(homeroomTeacherUser.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
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
            when(studentNoteService.findByStudentAndCategory(2L, null)).thenReturn(List.of());

            facade.getList(2L, null, admin);

            verify(studentNoteService).findByStudentAndCategory(2L, null);
        }

        @Test
        @DisplayName("TC-1-2. 담임 TEACHER")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(studentNoteService.findByStudentAndCategory(2L, null)).thenReturn(List.of());

            facade.getList(2L, null, teacher);

            verify(studentNoteService).findByStudentAndCategory(2L, null);
        }

        @Test
        @DisplayName("TC-1-3. 담임 아닌 TEACHER → STUDENT_ACCESS_DENIED, findByStudentAndCategory never")
        void nonHomeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubNonHomeroomTeacher(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, null, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(studentNoteService, never()).findByStudentAndCategory(any(), any())
            );
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-1-4. STUDENT·PARENT → STUDENT_ACCESS_DENIED")
        void nonTeacherRole(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);
            when(studentService.getById(2L)).thenReturn(student);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, null, userDetails));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("2. create()")
    class Create {

        private final CreateNoteRequest request = CreateNoteRequest.of(
                NoteCategory.ACHIEVEMENT, "수학 올림피아드 수상", LocalDate.of(2025, 3, 10));

        @Test
        @DisplayName("TC-2-1. 담임 TEACHER → userService.getById → save 순서, 응답 반환")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            User teacherUser = mock(User.class);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(userService.getById(10L)).thenReturn(teacherUser);
            when(studentNoteService.save(student, request, teacherUser)).thenReturn(note);
            when(note.getStudent()).thenReturn(student);
            when(note.getTeacher()).thenReturn(teacherUser);
            when(note.getCategory()).thenReturn(NoteCategory.ACHIEVEMENT);
            when(note.getContent()).thenReturn("수학 올림피아드 수상");
            when(note.getDate()).thenReturn(LocalDate.of(2025, 3, 10));

            var response = facade.create(2L, request, teacher);

            assertAll(
                    () -> verify(userService).getById(10L),
                    () -> verify(studentNoteService).save(student, request, teacherUser),
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
                    () -> verify(studentNoteService, never()).save(any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("3. update()")
    class Update {

        private final UpdateNoteRequest request = UpdateNoteRequest.of(
                NoteCategory.VOLUNTEER, "봉사 활동", LocalDate.of(2025, 4, 1));

        @Test
        @DisplayName("TC-3-1. 담임 TEACHER → getByIdAndStudentId → update 호출, 응답 반환")
        void homeroomTeacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(studentNoteService.getByIdAndStudentId(5L, 2L)).thenReturn(note);
            when(studentNoteService.update(note, request)).thenReturn(note);
            when(note.getStudent()).thenReturn(student);
            when(note.getTeacher()).thenReturn(mock(User.class));
            when(note.getCategory()).thenReturn(NoteCategory.VOLUNTEER);
            when(note.getContent()).thenReturn("봉사 활동");
            when(note.getDate()).thenReturn(LocalDate.of(2025, 4, 1));

            var response = facade.update(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(studentNoteService).getByIdAndStudentId(5L, 2L),
                    () -> verify(studentNoteService).update(note, request),
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
                    () -> verify(studentNoteService, never()).update(any(), any())
            );
        }

        @Test
        @DisplayName("TC-3-3. 노트 없음 → NOTE_NOT_FOUND, update never")
        void noteNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(studentNoteService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.NOTE_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 999L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(studentNoteService, never()).update(any(), any())
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
            when(studentNoteService.getByIdAndStudentId(5L, 2L)).thenReturn(note);

            facade.delete(2L, 5L, teacher);

            assertAll(
                    () -> verify(studentNoteService).getByIdAndStudentId(5L, 2L),
                    () -> verify(studentNoteService).delete(note)
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
                    () -> verify(studentNoteService, never()).delete(any())
            );
        }

        @Test
        @DisplayName("TC-4-3. 노트 없음 → NOTE_NOT_FOUND, delete never")
        void noteNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(student);
            stubStudent();
            stubHomeroomTeacher(10L);
            when(student.getId()).thenReturn(2L);
            when(studentNoteService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.NOTE_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 999L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(studentNoteService, never()).delete(any())
            );
        }
    }
}
