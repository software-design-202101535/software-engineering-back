package com.example.edumanager.domain.notification.listener;

import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.notification.event.FeedbackVisibilityChangedEvent;
import com.example.edumanager.domain.notification.event.GradeBatchProcessedEvent;
import com.example.edumanager.domain.notification.service.NotificationService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener 단위 테스트")
class NotificationEventListenerTest {

    @Mock NotificationService notificationService;
    @Mock StudentService studentService;

    @InjectMocks
    NotificationEventListener listener;

    @Mock User studentUser;
    @Mock User parentUser1;
    @Mock User parentUser2;
    @Mock StudentProfile student;

    @Nested
    @DisplayName("1. onGradeBatchProcessed()")
    class OnGradeBatchProcessed {

        @Test
        @DisplayName("TC-1-1. 성공 → 학생+학부모들 모두에게 GRADE_UPDATED 알림 생성")
        void withParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1, parentUser2));

            listener.onGradeBatchProcessed(GradeBatchProcessedEvent.of(10L, 3));

            verify(notificationService).createAll(
                    List.of(studentUser, parentUser1, parentUser2),
                    NotificationType.GRADE_UPDATED,
                    "성적이 업데이트되었습니다",
                    "성적 3건이 등록/수정되었습니다.",
                    10L,
                    ReferenceType.GRADE);
        }

        @Test
        @DisplayName("TC-1-2. 학부모 없음 → 학생에게만 알림")
        void withoutParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of());

            listener.onGradeBatchProcessed(GradeBatchProcessedEvent.of(10L, 1));

            verify(notificationService).createAll(
                    List.of(studentUser),
                    NotificationType.GRADE_UPDATED,
                    "성적이 업데이트되었습니다",
                    "성적 1건이 등록/수정되었습니다.",
                    10L,
                    ReferenceType.GRADE);
        }
    }

    @Nested
    @DisplayName("2. onFeedbackVisibilityChanged()")
    class OnFeedbackVisibilityChanged {

        @Test
        @DisplayName("TC-2-1. 신규 공개 대상 없음 → createAll 미호출")
        void noRecipient() {
            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, false, false, "ATTITUDE"));

            verify(notificationService, never()).createAll(any(), any(), any(), any(), anyLong(), any());
            verify(studentService, never()).getById(anyLong());
            verify(studentService, never()).getParentsByStudentId(anyLong());
        }

        @Test
        @DisplayName("TC-2-2. 학생만 신규 공개 → 학생에게만 알림")
        void studentOnly() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, true, false, "ATTITUDE"));

            verify(notificationService).createAll(
                    List.of(studentUser),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "ATTITUDE 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK);
            verify(studentService, never()).getParentsByStudentId(anyLong());
        }

        @Test
        @DisplayName("TC-2-3. 학생+학부모 모두 신규 공개 → 학생+학부모들 모두에게 알림")
        void bothStudentAndParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getUser()).thenReturn(studentUser);
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1));

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, true, true, "GRADE"));

            verify(notificationService).createAll(
                    List.of(studentUser, parentUser1),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "GRADE 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK);
        }

        @Test
        @DisplayName("TC-2-4. 학부모만 신규 공개 → 학부모들에게만 알림 (학생 조회 안 함)")
        void parentOnly() {
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1, parentUser2));

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, false, true, "BEHAVIOR"));

            verify(notificationService).createAll(
                    List.of(parentUser1, parentUser2),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "BEHAVIOR 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK);
            verify(studentService, never()).getById(anyLong());
        }
    }
}
