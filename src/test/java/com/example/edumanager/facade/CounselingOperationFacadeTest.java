package com.example.edumanager.facade;

import com.example.edumanager.domain.counseling.dto.CreateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.SharedCounselingResponse;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.edumanager.domain.counseling.entity.Counseling;
import com.example.edumanager.domain.counseling.service.CounselingService;
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
    @Mock User studentUser;
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

    private void stubSharedCounselingForResponse() {
        when(counseling.getId()).thenReturn(101L);
        when(counseling.getStudent()).thenReturn(studentProfile);
        when(studentProfile.getId()).thenReturn(12L);
        when(studentProfile.getUser()).thenReturn(studentUser);
        when(studentUser.getName()).thenReturn("김영희");
        when(studentProfile.getGrade()).thenReturn(1);
        when(studentProfile.getClassNum()).thenReturn(4);
        when(studentProfile.getNumber()).thenReturn(12);
        when(counseling.getTeacher()).thenReturn(teacherProfile);
        when(teacherProfile.getUser()).thenReturn(teacherUser);
        when(teacherUser.getId()).thenReturn(5L);
        when(teacherUser.getName()).thenReturn("김교사");
        when(counseling.getDate()).thenReturn(LocalDate.of(2026, 5, 20));
        when(counseling.getContent()).thenReturn("상담 내용");
        when(counseling.getNextPlan()).thenReturn("다음 계획");
        when(counseling.getNextDate()).thenReturn(LocalDate.of(2026, 6, 10));
        when(counseling.isSharedWithTeachers()).thenReturn(true);
        when(counseling.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 5, 20, 14, 30));
    }

    @Nested
    @DisplayName("0. getSharedList()")
    class GetSharedList {

        @Test
        @DisplayName("TC-0-1. 비TEACHER → STUDENT_ACCESS_DENIED, 어떤 서비스도 호출 안 함")
        void nonTeacher() {
            UserDetailsImpl student = UserDetailsImpl.create(1L, Role.STUDENT);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getSharedList(2026, null, null, null, null, student));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(teacherService, never()).getProfileByUserId(any()),
                    () -> verify(counselingService, never())
                            .findSharedForTeacher(any(), any(), anyInt(), any(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("TC-0-2. TEACHER → 요청자 school로 findSharedForTeacher 위임 + 매핑 결과 반환")
        void teacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(teacherService.getProfileByUserId(10L)).thenReturn(teacherProfile);
            when(teacherProfile.getSchool()).thenReturn(School.SUNRIN_HIGH_SCHOOL);
            when(counselingService.findSharedForTeacher(School.SUNRIN_HIGH_SCHOOL, 10L, 2026, 5, 1, 4, "김"))
                    .thenReturn(List.of(counseling));
            stubSharedCounselingForResponse();

            List<SharedCounselingResponse> result = facade.getSharedList(2026, 5, 1, 4, "김", teacher);

            SharedCounselingResponse first = result.get(0);
            assertAll(
                    () -> assertEquals(1, result.size()),
                    () -> verify(counselingService)
                            .findSharedForTeacher(School.SUNRIN_HIGH_SCHOOL, 10L, 2026, 5, 1, 4, "김"),
                    () -> assertEquals(101L, first.getId()),
                    () -> assertEquals(12L, first.getStudentId()),
                    () -> assertEquals("김영희", first.getStudentName()),
                    () -> assertEquals(1, first.getGrade()),
                    () -> assertEquals(4, first.getClassNum()),
                    () -> assertEquals(12, first.getNumber()),
                    () -> assertEquals(5L, first.getTeacherId()),
                    () -> assertEquals("김교사", first.getTeacherName())
            );
        }
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
