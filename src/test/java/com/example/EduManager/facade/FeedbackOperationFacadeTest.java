package com.example.EduManager.facade;

import com.example.EduManager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.EduManager.domain.feedback.entity.Feedback;
import com.example.EduManager.domain.feedback.entity.FeedbackCategory;
import com.example.EduManager.domain.feedback.service.FeedbackService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
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
@DisplayName("FeedbackOperationFacade 단위 테스트")
class FeedbackOperationFacadeTest {

    @Mock FeedbackService feedbackService;
    @Mock StudentService studentService;
    @Mock TeacherService teacherService;
    @Mock UserService userService;

    @InjectMocks FeedbackOperationFacade facade;

    @Mock StudentProfile studentProfile;
    @Mock TeacherProfile teacherProfile;
    @Mock User teacherUser;
    @Mock Feedback feedback;

    private void stubFeedbackForResponse() {
        when(feedback.getTeacher()).thenReturn(teacherProfile);
        when(teacherProfile.getUser()).thenReturn(teacherUser);
        when(feedback.getCategory()).thenReturn(FeedbackCategory.GRADE);
        when(feedback.getDate()).thenReturn(LocalDate.of(2025, 3, 14));
        when(feedback.getContent()).thenReturn("내용");
    }

    private void stubAuthor(Long authorUserId) {
        when(feedback.getTeacher()).thenReturn(teacherProfile);
        when(teacherProfile.getUser()).thenReturn(teacherUser);
        when(teacherUser.getId()).thenReturn(authorUserId);
    }

    @Nested
    @DisplayName("1. getList()")
    class GetList {

        @Test
        @DisplayName("TC-1-1. TEACHER, category=null → findAll 호출")
        void teacherNullCategory() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.findAll(2L)).thenReturn(List.of());

            facade.getList(2L, null, teacher);

            assertAll(
                    () -> verify(feedbackService).findAll(2L),
                    () -> verify(feedbackService, never()).findAllByCategory(any(), any())
            );
        }

        @Test
        @DisplayName("TC-1-2. TEACHER, category=GRADE → findAllByCategory 호출")
        void teacherWithCategory() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.findAllByCategory(2L, FeedbackCategory.GRADE)).thenReturn(List.of());

            facade.getList(2L, FeedbackCategory.GRADE, teacher);

            assertAll(
                    () -> verify(feedbackService).findAllByCategory(2L, FeedbackCategory.GRADE),
                    () -> verify(feedbackService, never()).findAll(any())
            );
        }

        @Test
        @DisplayName("TC-1-3. STUDENT(본인) → findStudentVisible 호출")
        void studentSelf() {
            UserDetailsImpl student = UserDetailsImpl.create(5L, Role.STUDENT);
            when(userService.getById(5L)).thenReturn(teacherUser);
            when(studentService.getProfileByUser(teacherUser)).thenReturn(studentProfile);
            when(studentProfile.getId()).thenReturn(2L);
            when(feedbackService.findStudentVisible(2L)).thenReturn(List.of());

            facade.getList(2L, null, student);

            verify(feedbackService).findStudentVisible(2L);
        }

        @Test
        @DisplayName("TC-1-4. STUDENT(타인 studentId) → STUDENT_ACCESS_DENIED, findStudentVisible never")
        void studentOther() {
            UserDetailsImpl student = UserDetailsImpl.create(5L, Role.STUDENT);
            when(userService.getById(5L)).thenReturn(teacherUser);
            when(studentService.getProfileByUser(teacherUser)).thenReturn(studentProfile);
            when(studentProfile.getId()).thenReturn(99L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, null, student));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).findStudentVisible(any())
            );
        }

        @Test
        @DisplayName("TC-1-5. PARENT(연결됨) → findParentVisible 호출")
        void parentLinked() {
            UserDetailsImpl parent = UserDetailsImpl.create(7L, Role.PARENT);
            User parentUser = mock(User.class);
            when(parentUser.getId()).thenReturn(7L);
            when(studentService.getParentsByStudentId(2L)).thenReturn(List.of(parentUser));
            when(feedbackService.findParentVisible(2L)).thenReturn(List.of());

            facade.getList(2L, null, parent);

            verify(feedbackService).findParentVisible(2L);
        }

        @Test
        @DisplayName("TC-1-6. PARENT(비연결) → STUDENT_ACCESS_DENIED, findParentVisible never")
        void parentNotLinked() {
            UserDetailsImpl parent = UserDetailsImpl.create(7L, Role.PARENT);
            when(studentService.getParentsByStudentId(2L)).thenReturn(List.of());

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.getList(2L, null, parent));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).findParentVisible(any())
            );
        }
    }

    @Nested
    @DisplayName("2. create()")
    class Create {

        private final CreateFeedbackRequest request = CreateFeedbackRequest.of(
                FeedbackCategory.GRADE, LocalDate.of(2025, 3, 14), "내용", true, false);

        @Test
        @DisplayName("TC-2-1. TEACHER → save 호출, 응답 반환")
        void teacher() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(studentService.getById(2L)).thenReturn(studentProfile);
            when(teacherService.getProfileByUserId(10L)).thenReturn(teacherProfile);
            when(feedbackService.save(studentProfile, teacherProfile, request)).thenReturn(feedback);
            stubFeedbackForResponse();

            var response = facade.create(2L, request, teacher);

            assertAll(
                    () -> verify(feedbackService).save(studentProfile, teacherProfile, request),
                    () -> assertNotNull(response)
            );
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"STUDENT", "PARENT"})
        @DisplayName("TC-2-2. STUDENT/PARENT → STUDENT_ACCESS_DENIED, save never")
        void nonTeacher(Role role) {
            UserDetailsImpl userDetails = UserDetailsImpl.create(1L, role);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.create(2L, request, userDetails));

            assertAll(
                    () -> assertEquals(ErrorCode.STUDENT_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).save(any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("3. update()")
    class Update {

        private final UpdateFeedbackRequest request = UpdateFeedbackRequest.of(
                FeedbackCategory.BEHAVIOR, LocalDate.of(2025, 3, 15), "수정 내용", true, true);

        @Test
        @DisplayName("TC-3-1. TEACHER(작성자) → update 호출, 응답 반환")
        void author() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);
            when(feedbackService.update(feedback, request)).thenReturn(feedback);
            stubFeedbackForResponse();

            var response = facade.update(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(feedbackService).update(feedback, request),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-3-2. TEACHER(타인 작성분) → FEEDBACK_ACCESS_DENIED, update never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 5L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).update(any(), any())
            );
        }

        @Test
        @DisplayName("TC-3-3. 피드백 없음 → FEEDBACK_NOT_FOUND, update never")
        void feedbackNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.FEEDBACK_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.update(2L, 999L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).update(any(), any())
            );
        }
    }

    @Nested
    @DisplayName("4. updateVisibility()")
    class UpdateVisibility {

        private final UpdateFeedbackVisibilityRequest request =
                UpdateFeedbackVisibilityRequest.of(false, true);

        @Test
        @DisplayName("TC-4-1. TEACHER(작성자) → updateVisibility 호출, 응답 반환")
        void author() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);
            when(feedbackService.updateVisibility(feedback, request)).thenReturn(feedback);
            stubFeedbackForResponse();

            var response = facade.updateVisibility(2L, 5L, request, teacher);

            assertAll(
                    () -> verify(feedbackService).updateVisibility(feedback, request),
                    () -> assertNotNull(response)
            );
        }

        @Test
        @DisplayName("TC-4-2. TEACHER(타인 작성분) → FEEDBACK_ACCESS_DENIED, updateVisibility never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.updateVisibility(2L, 5L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).updateVisibility(any(), any())
            );
        }

        @Test
        @DisplayName("TC-4-3. 피드백 없음 → FEEDBACK_NOT_FOUND, updateVisibility never")
        void feedbackNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.FEEDBACK_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.updateVisibility(2L, 999L, request, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).updateVisibility(any(), any())
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
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);

            facade.delete(2L, 5L, teacher);

            assertAll(
                    () -> verify(feedbackService).getByIdAndStudentId(5L, 2L),
                    () -> verify(feedbackService).delete(feedback)
            );
        }

        @Test
        @DisplayName("TC-5-2. TEACHER(타인 작성분) → FEEDBACK_ACCESS_DENIED, delete never")
        void nonAuthor() {
            UserDetailsImpl teacher = UserDetailsImpl.create(20L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 5L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_ACCESS_DENIED, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).delete(any())
            );
        }

        @Test
        @DisplayName("TC-5-3. 피드백 없음 → FEEDBACK_NOT_FOUND, delete never")
        void feedbackNotFound() {
            UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);
            when(feedbackService.getByIdAndStudentId(999L, 2L))
                    .thenThrow(new CustomException(ErrorCode.FEEDBACK_NOT_FOUND));

            CustomException ex = assertThrows(CustomException.class,
                    () -> facade.delete(2L, 999L, teacher));

            assertAll(
                    () -> assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(feedbackService, never()).delete(any())
            );
        }
    }
}
