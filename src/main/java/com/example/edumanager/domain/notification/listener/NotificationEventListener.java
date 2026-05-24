package com.example.edumanager.domain.notification.listener;

import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.notification.event.FeedbackVisibilityChangedEvent;
import com.example.edumanager.domain.notification.event.GradeBatchProcessedEvent;
import com.example.edumanager.domain.notification.service.NotificationService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final StudentService studentService;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGradeBatchProcessed(GradeBatchProcessedEvent event) {
        List<User> recipients = collectStudentAndParents(event.getStudentId());
        notificationService.createAll(
                recipients,
                NotificationType.GRADE_UPDATED,
                "성적이 업데이트되었습니다",
                String.format("성적 %d건이 등록/수정되었습니다.", event.getCount()),
                event.getStudentId(),
                ReferenceType.GRADE
        );
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackVisibilityChanged(FeedbackVisibilityChangedEvent event) {
        if (!event.hasRecipient()) return;
        List<User> recipients = collectVisibleRecipients(event);
        notificationService.createAll(
                recipients,
                NotificationType.FEEDBACK_SHARED,
                "새 피드백이 공유되었습니다",
                String.format("%s 피드백이 공유되었습니다.", event.getCategoryName()),
                event.getFeedbackId(),
                ReferenceType.FEEDBACK
        );
    }

    private List<User> collectStudentAndParents(Long studentId) {
        StudentProfile student = studentService.getById(studentId);
        List<User> recipients = new ArrayList<>();
        recipients.add(student.getUser());
        recipients.addAll(studentService.getParentsByStudentId(studentId));
        return recipients;
    }

    private List<User> collectVisibleRecipients(FeedbackVisibilityChangedEvent event) {
        List<User> recipients = new ArrayList<>();
        if (event.isNewlyVisibleToStudent()) {
            recipients.add(studentService.getById(event.getStudentId()).getUser());
        }
        if (event.isNewlyVisibleToParent()) {
            recipients.addAll(studentService.getParentsByStudentId(event.getStudentId()));
        }
        return recipients;
    }
}
