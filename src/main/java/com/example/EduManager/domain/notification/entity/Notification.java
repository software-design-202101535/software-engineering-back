package com.example.EduManager.domain.notification.entity;

import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReferenceType referenceType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification of(User user, NotificationType type, String title, String message,
                                  Long referenceId, ReferenceType referenceType) {
        Notification notification = new Notification();
        notification.user = user;
        notification.type = type;
        notification.title = title;
        notification.message = message;
        notification.isRead = false;
        notification.referenceId = referenceId;
        notification.referenceType = referenceType;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
