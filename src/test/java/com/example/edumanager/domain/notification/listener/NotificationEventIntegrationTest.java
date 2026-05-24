package com.example.edumanager.domain.notification.listener;

import com.example.edumanager.domain.notification.entity.Notification;
import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.event.GradeBatchProcessedEvent;
import com.example.edumanager.domain.notification.repository.NotificationRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.global.config.AsyncConfig;
import com.example.edumanager.global.fcm.FcmClient;
import com.example.edumanager.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("Notification 이벤트 통합 테스트 (AFTER_COMMIT 경계)")
@Import(NotificationEventIntegrationTest.SyncExecutorConfig.class)
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class NotificationEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired ApplicationEventPublisher eventPublisher;
    @Autowired NotificationRepository notificationRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @MockitoBean FcmClient fcmClient;

    @Test
    @DisplayName("TC-E-1. 트랜잭션 commit 시 AFTER_COMMIT listener 호출 → DB 알림 생성 + FCM send 호출")
    void afterCommit_onCommit_createsNotificationsAndPushes() {
        StudentProfile student = insertStudent("e1@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 1, 1);

        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.executeWithoutResult(status ->
                eventPublisher.publishEvent(GradeBatchProcessedEvent.of(student.getId(), 3)));

        List<Notification> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(student.getUser().getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.GRADE_UPDATED);
        assertThat(notifications.get(0).getMessage()).isEqualTo("성적 3건이 등록/수정되었습니다.");
        verify(fcmClient).send(any(), eq("성적이 업데이트되었습니다"), eq("성적 3건이 등록/수정되었습니다."));
    }

    @Test
    @DisplayName("TC-E-2. 트랜잭션 rollback 시 AFTER_COMMIT listener 미호출 → DB 알림 없음 + FCM 미호출")
    void afterCommit_onRollback_listenerSkipped() {
        StudentProfile student = insertStudent("e2@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 1, 2);

        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.executeWithoutResult(status -> {
            eventPublisher.publishEvent(GradeBatchProcessedEvent.of(student.getId(), 3));
            status.setRollbackOnly();
        });

        assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(student.getUser().getId()))
                .isEmpty();
        verifyNoInteractions(fcmClient);
    }

    @TestConfiguration
    static class SyncExecutorConfig {
        @Bean(name = AsyncConfig.NOTIFICATION_EXECUTOR)
        @Primary
        public Executor syncNotificationExecutor() {
            return new SyncTaskExecutor();
        }
    }
}
