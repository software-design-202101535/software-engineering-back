package com.example.EduManager.facade;

import com.example.EduManager.domain.counseling.dto.CreateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.EduManager.domain.counseling.entity.Counseling;
import com.example.EduManager.domain.counseling.service.CounselingService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CounselingOperationFacade 단위 테스트")
class CounselingOperationFacadeTest {

    @Mock CounselingService counselingService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;

    @InjectMocks CounselingOperationFacade facade;

    @Mock StudentProfile studentProfile;
    @Mock TeacherProfile teacherProfile;
    @Mock User teacherUser;
    @Mock Counseling counseling;

    private void stubCounselingForResponse() {
        when(counseling.getId()).thenReturn(1L);
        when(counseling.getStudent()).thenReturn(studentProfile);
        when(studentProfile.getId()).thenReturn(2L);
        when(counseling.getTeacher()).thenReturn(teacherProfile);
        when(teacherProfile.getUser()).thenReturn(teacherUser);
        when(teacherUser.getId()).thenReturn(10L);
        when(teacherUser.getName()).thenReturn("김선생");
        when(counseling.getDate()).thenReturn(LocalDate.of(2026, 3, 15));
        when(counseling.getContent()).thenReturn("내용");
        when(counseling.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 3, 15, 14, 30));
    }

    private void stubAuthor(Long authorUserId) {
        when(counseling.getTeacher()).thenReturn(teacherProfile);
        when(teacherProfile.getUser()).thenReturn(teacherUser);
        when(teacherUser.getId()).thenReturn(authorUserId);
    }

    @Nested
    @DisplayName("1. getList()")
    class GetList {

        @Test
        @DisplayName("TC-1-1. month=null → findForTeacherByYear 호출")
        void monthNull() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(counselingService.findForTeacherByYear(2L, 10L, 2026)).thenReturn(List.of());

            facade.getList(2L, 2026, null, teacher);

            assertAll(
                    () -> verify(counselingService).findForTeacherByYear(2L, 10L, 2026),
                    () -> verify(counselingService, never()).findForTeacherByYearAndMonth(any(), any(), anyInt(), anyInt())
            );
        }

        @Test
        @DisplayName("TC-1-2. month=3 → findForTeacherByYearAndMonth 호출")
        void monthGiven() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(counselingService.findForTeacherByYearAndMonth(2L, 10L, 2026, 3)).thenReturn(List.of());

            facade.getList(2L, 2026, 3, teacher);

            assertAll(
                    () -> verify(counselingService).findForTeacherByYearAndMonth(2L, 10L, 2026, 3),
                    () -> verify(counselingService, never()).findForTeacherByYear(any(), any(), anyInt())
            );
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-1-3. STUDENT/PARENT → STUDENT_ACCESS_DENIED")
        void nonTeacher(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, 2026, null, userDetails));

            assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("2. create()")
    class Create {

        private final CreateCounselingRequest request = CreateCounselingRequest.of(
                LocalDate.of(2026, 3, 15), "내용", null, null, false);

        @Test
        @DisplayName("TC-2-1. TEACHER → save 호출, 응답 반환")
        void teacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(studentProfile);
            when(teacherService.getProfileByUserId(10L)).thenReturn(teacherProfile);
            when(counselingService.save(studentProfile, teacherProfile, request)).thenReturn(counseling);
            stubCounselingForResponse();

            var response = facade.create(2L, request, teacher);

            assertAll(
                    () -> verify(counselingService).save(studentProfile, teacherProfile, request),
                    () -> assertNotNull(response)
            );
        }
    }

    @Nested
    @DisplayName("3. update()")
    class Update {

        private final UpdateCounselingRequest request = UpdateCounselingRequest.of(
                LocalDate.of(2026, 3, 15), "수정 내용", null, null, false);

        @Test
        @DisplayName("TC-3-1. TEACHER(작성자) → update 호출, 응답 반환")
        void author() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);
            when(counselingService.update(counseling, request)).thenReturn(counseling);
            stubCounselingForResponse();

            var response = facade.update(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(counselingService).update(counseling, request),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-3-2. TEACHER(타인 작성분) → COUNSELING_ACCESS_DENIED, update never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 5L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.COUNSELING_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(counselingService, never()).update(any(), any())
            );
        }
    }

    @Nested
    @DisplayName("4. updateShare()")
    class UpdateShare {

        private final UpdateCounselingShareRequest request = UpdateCounselingShareRequest.of(true);

        @Test
        @DisplayName("TC-4-1. TEACHER(작성자) → updateSharedStatus 호출, 응답 반환")
        void author() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);
            when(counselingService.updateSharedStatus(counseling, request)).thenReturn(counseling);
            stubCounselingForResponse();

            var response = facade.updateShare(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(counselingService).updateSharedStatus(counseling, request),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-4-2. TEACHER(타인 작성분) → COUNSELING_ACCESS_DENIED, updateSharedStatus never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.updateShare(2L, 5L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.COUNSELING_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(counselingService, never()).updateSharedStatus(any(), any())
            );
        }
    }

    @Nested
    @DisplayName("5. delete()")
    class Delete {

        @Test
        @DisplayName("TC-5-1. TEACHER(작성자) → delete 호출")
        void author() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);

            facade.delete(2L, 5L, teacher);

            assertAll(
                    () -> verify(counselingService).getByIdAndStudentId(5L, 2L),
                    () -> verify(counselingService).delete(counseling)
            );
        }

        @Test
        @DisplayName("TC-5-2. TEACHER(타인 작성분) → COUNSELING_ACCESS_DENIED, delete never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(counselingService.getByIdAndStudentId(5L, 2L)).thenReturn(counseling);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 5L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.COUNSELING_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(counselingService, never()).delete(any())
            );
        }
    }
}
