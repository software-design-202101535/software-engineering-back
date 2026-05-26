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
import com.example.edumanager.global.config.AsyncConfig;
import com.example.edumanager.global.fcm.FcmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final StudentService studentService;
    private final DeviceTokenService deviceTokenService;
    private final FcmClient fcmClient;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGradeBatchProcessed(GradeBatchProcessedEvent event) {
        StudentProfile student = studentService.getById(event.getStudentId());
        List<User> recipients = collectStudentAndParents(student);
        if (recipients.isEmpty()) return;
        notifyAll(
                recipients,
                NotificationType.GRADE_UPDATED,
                "성적이 업데이트되었습니다",
                String.format("성적 %d건이 등록/수정되었습니다.", event.getCount()),
                event.getStudentId(),
                ReferenceType.GRADE,
                student);
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent event) {
        if (!event.hasRecipient()) return;
        StudentProfile student = studentService.getById(event.getStudentId());
        List<User> recipients = collectVisibleRecipients(event, student);
        if (recipients.isEmpty()) return;
        notifyAll(
                recipients,
                NotificationType.FEEDBACK_SHARED,
                "새 피드백이 공유되었습니다",
                String.format("%s 피드백이 공유되었습니다.", event.getCategoryName()),
                event.getFeedbackId(),
                ReferenceType.FEEDBACK,
                student);
    }

    private void notifyAll(List<User> recipients, NotificationType type, String title, String body,
                           Long referenceId, ReferenceType referenceType, StudentProfile student) {
        notificationService.createAll(recipients, type, title, body, referenceId, referenceType,
                student.getId(), student.getUser().getName());
        schedulePushAfterCommit(recipients, title, body);
    }

    private void schedulePushAfterCommit(List<User> recipients, String title, String body) {
        List<String> tokens = resolveTokens(recipients);
        if (tokens.isEmpty()) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            fcmClient.send(tokens, title, body);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fcmClient.send(tokens, title, body);
            }
        });
    }

    private List<String> resolveTokens(List<User> recipients) {
        List<Long> userIds = recipients.stream().map(User::getId).toList();
        return deviceTokenService.findTokensByUserIds(userIds);
    }

    private List<User> collectStudentAndParents(StudentProfile student) {
        List<User> recipients = new ArrayList<>();
        recipients.add(student.getUser());
        recipients.addAll(studentService.getParentsByStudentId(student.getId()));
        return recipients;
    }

    private List<User> collectVisibleRecipients(FeedbackVisibilityChangedEvent event, StudentProfile student) {
        List<User> recipients = new ArrayList<>();
        if (event.isNewlyVisibleToStudent()) {
            recipients.add(student.getUser());
        }
        if (event.isNewlyVisibleToParent()) {
            recipients.addAll(studentService.getParentsByStudentId(student.getId()));
        }
        return recipients;
    }
}
