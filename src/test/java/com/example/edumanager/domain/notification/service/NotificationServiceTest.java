package com.example.edumanager.domain.notification.service;

import com.example.edumanager.domain.notification.entity.Notification;
import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.notification.repository.NotificationRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;

    @Nested
    @DisplayName("1. findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("TC-1-1. 성공 → repository.findByUserIdOrderByCreatedAtDescIdDesc 위임, 결과 반환")
        void success() {
            Notification n1 = mock(Notification.class);
            Notification n2 = mock(Notification.class);
            when(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(1L))
                    .thenReturn(List.of(n1, n2));

            List<Notification> result = notificationService.findByUserId(1L);

            assertAll(
                    () -> verify(notificationRepository).findByUserIdOrderByCreatedAtDescIdDesc(1L),
                    () -> assertEquals(List.of(n1, n2), result)
            );
        }
    }

    @Nested
    @DisplayName("2. markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("TC-2-1. 알림이 없거나 본인의 알림이 아님 → NOTIFICATION_NOT_FOUND, markAsRead 미호출")
        void notFound() {
            Notification other = mock(Notification.class);
            when(notificationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> notificationService.markAsRead(99L, 1L));

            assertAll(
                    () -> assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode()),
                    () -> verify(other, never()).markAsRead()
            );
        }

        @Test
        @DisplayName("TC-2-2. 성공 → notification.markAsRead() 호출")
        void success() {
            Notification notification = mock(Notification.class);
            when(notificationRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(notification));

            notificationService.markAsRead(10L, 1L);

            verify(notification).markAsRead();
        }
    }

    @Nested
    @DisplayName("3. markAllAsRead()")
    class MarkAllAsRead {

        @Test
        @DisplayName("TC-3-1. 성공 → repository.markAllAsReadByUserId 위임, 반환값 그대로")
        void success() {
            when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(5);

            int updated = notificationService.markAllAsRead(1L);

            assertAll(
                    () -> verify(notificationRepository).markAllAsReadByUserId(1L),
                    () -> assertEquals(5, updated)
            );
        }
    }

    @Nested
    @DisplayName("4. createAll()")
    class CreateAll {

        @Test
        @DisplayName("TC-4-1. 수신자가 비어있음 → saveAll 미호출, 빈 리스트 반환")
        void emptyRecipients() {
            List<Notification> result = notificationService.createAll(
                    List.of(), NotificationType.GRADE_UPDATED, "t", "m", 1L, ReferenceType.GRADE,
                    1L, "홍길동");

            assertAll(
                    () -> assertTrue(result.isEmpty()),
                    () -> verify(notificationRepository, never()).saveAll(any())
            );
        }

        @Test
        @DisplayName("TC-4-2. 성공 → 각 수신자별 Notification 생성 후 saveAll 호출")
        void success() {
            User user1 = mock(User.class);
            User user2 = mock(User.class);
            when(notificationRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            notificationService.createAll(
                    List.of(user1, user2),
                    NotificationType.FEEDBACK_SHARED,
                    "title",
                    "message",
                    7L,
                    ReferenceType.FEEDBACK,
                    9L,
                    "홍길동");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificationRepository).saveAll(captor.capture());
            List<Notification> saved = captor.getValue();
            assertAll(
                    () -> assertEquals(2, saved.size()),
                    () -> assertEquals(user1, saved.get(0).getUser()),
                    () -> assertEquals(user2, saved.get(1).getUser()),
                    () -> assertEquals(NotificationType.FEEDBACK_SHARED, saved.get(0).getType()),
                    () -> assertEquals("title", saved.get(0).getTitle()),
                    () -> assertEquals("message", saved.get(0).getMessage()),
                    () -> assertEquals(7L, saved.get(0).getReferenceId()),
                    () -> assertEquals(ReferenceType.FEEDBACK, saved.get(0).getReferenceType()),
                    () -> assertEquals(9L, saved.get(0).getReferenceStudentId()),
                    () -> assertEquals("홍길동", saved.get(0).getReferenceStudentName())
            );
        }
    }
}
