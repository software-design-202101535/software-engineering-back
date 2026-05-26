package com.example.edumanager.domain.notification.listener;

import com.example.edumanager.domain.devicetoken.service.DeviceTokenService;
import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.notification.event.FeedbackVisibilityChangedEvent;
import com.example.edumanager.domain.notification.event.GradeBatchProcessedEvent;
import com.example.edumanager.domain.notification.service.NotificationService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.fcm.FcmClient;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener 단위 테스트")
class NotificationEventListenerTest {

    @Mock NotificationService notificationService;
    @Mock StudentService studentService;
    @Mock DeviceTokenService deviceTokenService;
    @Mock FcmClient fcmClient;

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
        @DisplayName("TC-1-1. 학생+학부모들 → DB 알림 저장 + FCM push 발송")
        void withParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getName()).thenReturn("홍길동");
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1, parentUser2));
            when(studentUser.getId()).thenReturn(100L);
            when(parentUser1.getId()).thenReturn(200L);
            when(parentUser2.getId()).thenReturn(201L);
            when(deviceTokenService.findTokensByUserIds(List.of(100L, 200L, 201L)))
                    .thenReturn(List.of("tok-s", "tok-p1", "tok-p2"));

            listener.onGradeBatchProcessed(GradeBatchProcessedEvent.of(10L, 3));

            verify(notificationService).createAll(
                    List.of(studentUser, parentUser1, parentUser2),
                    NotificationType.GRADE_UPDATED,
                    "성적이 업데이트되었습니다",
                    "성적 3건이 등록/수정되었습니다.",
                    10L,
                    ReferenceType.GRADE,
                    10L,
                    "홍길동");
            verify(fcmClient).send(
                    List.of("tok-s", "tok-p1", "tok-p2"),
                    "성적이 업데이트되었습니다",
                    "성적 3건이 등록/수정되었습니다.");
        }

        @Test
        @DisplayName("TC-1-2. 학부모 없음 → 학생에게만 알림+push")
        void withoutParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getName()).thenReturn("홍길동");
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of());
            when(studentUser.getId()).thenReturn(100L);
            when(deviceTokenService.findTokensByUserIds(List.of(100L))).thenReturn(List.of("tok-s"));

            listener.onGradeBatchProcessed(GradeBatchProcessedEvent.of(10L, 1));

            verify(notificationService).createAll(
                    List.of(studentUser),
                    NotificationType.GRADE_UPDATED,
                    "성적이 업데이트되었습니다",
                    "성적 1건이 등록/수정되었습니다.",
                    10L,
                    ReferenceType.GRADE,
                    10L,
                    "홍길동");
            verify(fcmClient).send(
                    List.of("tok-s"),
                    "성적이 업데이트되었습니다",
                    "성적 1건이 등록/수정되었습니다.");
        }
    }

    @Nested
    @DisplayName("2. onFeedbackVisibilityChanged()")
    class OnFeedbackVisibilityChanged {

        @Test
        @DisplayName("TC-2-1. 신규 공개 대상 없음 → 어떤 처리도 안 함")
        void noRecipient() {
            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, false, false, "ATTITUDE"));

            verify(notificationService, never()).createAll(any(), any(), any(), any(), anyLong(), any(), anyLong(), any());
            verify(fcmClient, never()).send(any(), anyString(), anyString());
            verify(studentService, never()).getById(anyLong());
        }

        @Test
        @DisplayName("TC-2-2. 학생만 신규 공개 → 학생만 알림+push")
        void studentOnly() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getName()).thenReturn("홍길동");
            when(studentUser.getId()).thenReturn(100L);
            when(deviceTokenService.findTokensByUserIds(List.of(100L))).thenReturn(List.of("tok-s"));

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, true, false, "ATTITUDE"));

            verify(notificationService).createAll(
                    List.of(studentUser),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "ATTITUDE 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK,
                    10L,
                    "홍길동");
            verify(fcmClient).send(
                    List.of("tok-s"),
                    "새 피드백이 공유되었습니다",
                    "ATTITUDE 피드백이 공유되었습니다.");
            verify(studentService, never()).getParentsByStudentId(anyLong());
        }

        @Test
        @DisplayName("TC-2-3. 학생+학부모 모두 신규 공개 → 둘 다 알림+push")
        void bothStudentAndParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getName()).thenReturn("홍길동");
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1));
            when(studentUser.getId()).thenReturn(100L);
            when(parentUser1.getId()).thenReturn(200L);
            when(deviceTokenService.findTokensByUserIds(List.of(100L, 200L)))
                    .thenReturn(List.of("tok-s", "tok-p1"));

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, true, true, "GRADE"));

            verify(notificationService).createAll(
                    List.of(studentUser, parentUser1),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "GRADE 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK,
                    10L,
                    "홍길동");
            verify(fcmClient).send(
                    List.of("tok-s", "tok-p1"),
                    "새 피드백이 공유되었습니다",
                    "GRADE 피드백이 공유되었습니다.");
        }

        @Test
        @DisplayName("TC-2-5. 학부모만 신규 공개이지만 학부모 0명 → createAll/fcm 모두 미호출")
        void parentOnlyButNoParents() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of());

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, false, true, "ATTITUDE"));

            verify(notificationService, never()).createAll(any(), any(), any(), any(), anyLong(), any(), anyLong(), any());
            verify(fcmClient, never()).send(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("TC-2-4. 학부모만 신규 공개 → 학부모들만 알림+push")
        void parentOnly() {
            when(studentService.getById(10L)).thenReturn(student);
            when(student.getId()).thenReturn(10L);
            when(student.getUser()).thenReturn(studentUser);
            when(studentUser.getName()).thenReturn("홍길동");
            when(studentService.getParentsByStudentId(10L)).thenReturn(List.of(parentUser1, parentUser2));
            when(parentUser1.getId()).thenReturn(200L);
            when(parentUser2.getId()).thenReturn(201L);
            when(deviceTokenService.findTokensByUserIds(List.of(200L, 201L)))
                    .thenReturn(List.of("tok-p1", "tok-p2"));

            listener.onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent.of(
                    5L, 10L, false, true, "BEHAVIOR"));

            verify(notificationService).createAll(
                    List.of(parentUser1, parentUser2),
                    NotificationType.FEEDBACK_SHARED,
                    "새 피드백이 공유되었습니다",
                    "BEHAVIOR 피드백이 공유되었습니다.",
                    5L,
                    ReferenceType.FEEDBACK,
                    10L,
                    "홍길동");
            verify(fcmClient).send(
                    List.of("tok-p1", "tok-p2"),
                    "새 피드백이 공유되었습니다",
                    "BEHAVIOR 피드백이 공유되었습니다.");
        }
    }
}
