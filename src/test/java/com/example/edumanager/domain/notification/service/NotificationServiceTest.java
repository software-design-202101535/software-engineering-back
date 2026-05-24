package com.example.edumanager.domain.notification.service;

import com.example.edumanager.domain.notification.entity.Notification;
import com.example.edumanager.domain.notification.repository.NotificationRepository;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        @DisplayName("TC-1-1. 성공 → repository.findByUserIdOrderByCreatedAtDesc 위임, 결과 반환")
        void success() {
            Notification n1 = mock(Notification.class);
            Notification n2 = mock(Notification.class);
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(n1, n2));

            List<Notification> result = notificationService.findByUserId(1L);

            assertAll(
                    () -> verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L),
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
}
