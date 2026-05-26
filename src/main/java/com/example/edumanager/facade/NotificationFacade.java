package com.example.edumanager.facade;

import com.example.edumanager.domain.notification.dto.NotificationResponse;
import com.example.edumanager.domain.notification.service.NotificationService;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UserDetailsImpl userDetails) {
        return notificationService.findByUserId(userDetails.getUserId()).stream()
                .map(NotificationResponse::of)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, UserDetailsImpl userDetails) {
        notificationService.markAsRead(notificationId, userDetails.getUserId());
    }

    @Transactional
    public void markAllAsRead(UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getUserId());
    }
}
