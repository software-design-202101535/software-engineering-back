package com.example.edumanager.facade;

import com.example.edumanager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.edumanager.domain.feedback.entity.Feedback;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import com.example.edumanager.domain.feedback.service.FeedbackService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.domain.notification.event.FeedbackSharedEvent;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    @Mock ApplicationEventPublisher eventPublisher;

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
        @DisplayName("TC-1-5-1. STUDENT(본인), category=GRADE → findStudentVisibleByCategory 호출")
        void studentSelfWithCategory() {
            UserDetailsImpl student = UserDetailsImpl.create(5L, Role.STUDENT);
            when(userService.getById(5L)).thenReturn(teacherUser);
            when(studentService.getProfileByUser(teacherUser)).thenReturn(studentProfile);
            when(studentProfile.getId()).thenReturn(2L);
            when(feedbackService.findStudentVisibleByCategory(2L, FeedbackCategory.GRADE)).thenReturn(List.of());

            facade.getList(2L, FeedbackCategory.GRADE, student);

            assertAll(
                    () -> verify(feedbackService).findStudentVisibleByCategory(2L, FeedbackCategory.GRADE),
                    () -> verify(feedbackService, never()).findStudentVisible(any())
            );
        }

        @Test
        @DisplayName("TC-1-5-2. PARENT(연결됨), category=GRADE → findParentVisibleByCategory 호출")
        void parentLinkedWithCategory() {
            UserDetailsImpl parent = UserDetailsImpl.create(7L, Role.PARENT);
            User parentUser = mock(User.class);
            when(parentUser.getId()).thenReturn(7L);
            when(studentService.getParentsByStudentId(2L)).thenReturn(List.of(parentUser));
            when(feedbackService.findParentVisibleByCategory(2L, FeedbackCategory.GRADE)).thenReturn(List.of());

            facade.getList(2L, FeedbackCategory.GRADE, parent);

            assertAll(
                    () -> verify(feedbackService).findParentVisibleByCategory(2L, FeedbackCategory.GRADE),
                    () -> verify(feedbackService, never()).findParentVisible(any())
            );
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
    @DisplayName("2-P. create() publishEvent 분기")
    class CreatePublish {

        private final UserDetailsImpl teacher = UserDetailsImpl.create(10L, Role.TEACHER);

        private CreateFeedbackRequest request(boolean studentVisible, boolean parentVisible) {
            return CreateFeedbackRequest.of(
                    FeedbackCategory.GRADE, LocalDate.of(2025, 3, 14), "내용", studentVisible, parentVisible);
        }

        private void stubCreate(boolean studentVisible, boolean parentVisible) {
            when(studentService.getById(2L)).thenReturn(studentProfile);
            when(teacherService.getProfileByUserId(10L)).thenReturn(teacherProfile);
            when(feedbackService.save(eq(studentProfile), eq(teacherProfile), any())).thenReturn(feedback);
            when(feedback.getId()).thenReturn(5L);
            when(feedback.isStudentVisible()).thenReturn(studentVisible);
            when(feedback.isParentVisible()).thenReturn(parentVisible);
            stubFeedbackForResponse();
        }

        @ParameterizedTest(name = "student={0}, parent={1}")
        @CsvSource({"true, true", "true, false", "false, true"})
        @DisplayName("TC-2P-1. 공개 상태로 생성 → publish (생성 시 공개값이 곧 newlyVisible)")
        void publishesWhenCreatedVisible(boolean studentVisible, boolean parentVisible) {
            stubCreate(studentVisible, parentVisible);

            facade.create(2L, request(studentVisible, parentVisible), teacher);

            ArgumentCaptor<FeedbackSharedEvent> captor =
                    ArgumentCaptor.forClass(FeedbackSharedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            FeedbackSharedEvent event = captor.getValue();
            assertAll(
                    () -> assertEquals(5L, event.getFeedbackId()),
                    () -> assertEquals(2L, event.getStudentId()),
                    () -> assertEquals(studentVisible, event.isNewlyVisibleToStudent()),
                    () -> assertEquals(parentVisible, event.isNewlyVisibleToParent()),
                    () -> assertEquals("GRADE", event.getCategoryName())
            );
        }

        @Test
        @DisplayName("TC-2P-2. 비공개로 생성 (false/false) → publish 없음")
        void noPublishWhenCreatedHidden() {
            stubCreate(false, false);

            facade.create(2L, request(false, false), teacher);

            verify(eventPublisher, never()).publishEvent(any());
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
    @DisplayName("4-P. updateVisibility() publishEvent 분기")
    class UpdateVisibilityPublish {

        private UserDetailsImpl teacher() {
            return UserDetailsImpl.create(10L, Role.TEACHER);
        }

        private void stubAuthorAndUpdate(boolean wasStu, boolean wasPar, boolean nowStu, boolean nowPar) {
            when(feedbackService.getByIdAndStudentId(5L, 2L)).thenReturn(feedback);
            stubAuthor(10L);
            when(feedback.isStudentVisible()).thenReturn(wasStu, nowStu);
            when(feedback.isParentVisible()).thenReturn(wasPar, nowPar);
            when(feedbackService.updateVisibility(eq(feedback), any())).thenReturn(feedback);
        }

        private void stubFeedbackForEvent() {
            when(feedback.getId()).thenReturn(5L);
            when(feedback.getCategory()).thenReturn(FeedbackCategory.GRADE);
        }

        private FeedbackSharedEvent capturePublished() {
            ArgumentCaptor<FeedbackSharedEvent> captor =
                    ArgumentCaptor.forClass(FeedbackSharedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            return captor.getValue();
        }

        @Test
        @DisplayName("TC-4P-1. 학생/학부모 둘 다 false→true → publish (s=true, p=true)")
        void bothNewlyShared() {
            stubAuthorAndUpdate(false, false, true, true);
            stubFeedbackForEvent();

            facade.updateVisibility(2L, 5L, UpdateFeedbackVisibilityRequest.of(true, true), teacher());

            FeedbackSharedEvent event = capturePublished();
            assertAll(
                    () -> assertEquals(5L, event.getFeedbackId()),
                    () -> assertEquals(2L, event.getStudentId()),
                    () -> assertTrue(event.isNewlyVisibleToStudent()),
                    () -> assertTrue(event.isNewlyVisibleToParent()),
                    () -> assertEquals("GRADE", event.getCategoryName())
            );
        }

        @Test
        @DisplayName("TC-4P-2. 학생만 false→true, 학부모 false→false → publish (s=true, p=false)")
        void onlyStudentNewlyShared() {
            stubAuthorAndUpdate(false, false, true, false);
            stubFeedbackForEvent();

            facade.updateVisibility(2L, 5L, UpdateFeedbackVisibilityRequest.of(true, false), teacher());

            FeedbackSharedEvent event = capturePublished();
            assertAll(
                    () -> assertTrue(event.isNewlyVisibleToStudent()),
                    () -> assertFalse(event.isNewlyVisibleToParent())
            );
        }

        @Test
        @DisplayName("TC-4P-3. 학부모만 false→true → publish (s=false, p=true)")
        void onlyParentNewlyShared() {
            stubAuthorAndUpdate(false, false, false, true);
            stubFeedbackForEvent();

            facade.updateVisibility(2L, 5L, UpdateFeedbackVisibilityRequest.of(false, true), teacher());

            FeedbackSharedEvent event = capturePublished();
            assertAll(
                    () -> assertFalse(event.isNewlyVisibleToStudent()),
                    () -> assertTrue(event.isNewlyVisibleToParent())
            );
        }

        @Test
        @DisplayName("TC-4P-4. 이미 둘 다 공개됨 (true→true) → publish 없음")
        void alreadyVisibleNoPublish() {
            stubAuthorAndUpdate(true, true, true, true);

            facade.updateVisibility(2L, 5L, UpdateFeedbackVisibilityRequest.of(true, true), teacher());

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("TC-4P-5. 가시성 축소 (true→false) → publish 없음")
        void visibilityShrunkNoPublish() {
            stubAuthorAndUpdate(true, true, false, false);

            facade.updateVisibility(2L, 5L, UpdateFeedbackVisibilityRequest.of(false, false), teacher());

            verify(eventPublisher, never()).publishEvent(any());
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
