package com.example.EduManager.domain.feedback.entity;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isShared;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Feedback of(StudentProfile student, User teacher, FeedbackCategory category, String content) {
        Feedback feedback = new Feedback();
        feedback.student = student;
        feedback.teacher = teacher;
        feedback.category = category;
        feedback.content = content;
        feedback.isShared = false;
        feedback.createdAt = LocalDateTime.now();
        feedback.updatedAt = LocalDateTime.now();
        return feedback;
    }

    public void update(FeedbackCategory category, String content) {
        this.category = category;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void share() {
        this.isShared = true;
        this.updatedAt = LocalDateTime.now();
    }
}
