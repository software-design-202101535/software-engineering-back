package com.example.edumanager.domain.notification.service;

import com.example.edumanager.domain.notification.entity.Notification;
import com.example.edumanager.domain.notification.entity.NotificationType;
import com.example.edumanager.domain.notification.entity.ReferenceType;
import com.example.edumanager.domain.notification.repository.NotificationRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public List<Notification> createAll(List<User> users, NotificationType type, String title, String message,
                                        Long referenceId, ReferenceType referenceType) {
        if (users.isEmpty()) return List.of();
        List<Notification> notifications = users.stream()
                .map(user -> Notification.of(user, type, title, message, referenceId, referenceType))
                .toList();
        return notificationRepository.saveAll(notifications);
    }
}
