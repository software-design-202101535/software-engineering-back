package com.example.edumanager.domain.notification.controller;

import com.example.edumanager.domain.notification.dto.NotificationResponse;
import com.example.edumanager.domain.notification.service.NotificationService;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.NotificationApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApiSpecification {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                notificationService.findByUserId(userDetails.getUserId()).stream()
                        .map(NotificationResponse::of)
                        .toList()
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAsRead(notificationId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAllAsRead(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
