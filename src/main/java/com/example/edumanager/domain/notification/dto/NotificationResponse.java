package com.example.edumanager.domain.notification.dto;

import com.example.edumanager.domain.notification.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private Long referenceId;
    private String referenceType;
    private Long referenceStudentId;
    private String referenceStudentName;
    private LocalDateTime createdAt;

    private NotificationResponse() {
    }

    public static NotificationResponse ofForTest(Long id, String type, boolean isRead) {
        NotificationResponse response = new NotificationResponse();
        response.id = id;
        response.type = type;
        response.isRead = isRead;
        return response;
    }

    public static NotificationResponse of(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.id = notification.getId();
        response.type = notification.getType().name();
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.isRead = notification.isRead();
        response.referenceId = notification.getReferenceId();
        response.referenceType = notification.getReferenceType() == null
                ? null
                : notification.getReferenceType().name();
        response.referenceStudentId = notification.getReferenceStudentId();
        response.referenceStudentName = notification.getReferenceStudentName();
        response.createdAt = notification.getCreatedAt();
        return response;
    }
}
