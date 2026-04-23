package com.example.EduManager.domain.feedback.entity;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@EntityListeners(AuditingEntityListener.class)
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
    private TeacherProfile teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackCategory category;

    @Column(name = "feedback_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean studentVisible;

    @Column(nullable = false)
    private boolean parentVisible;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Feedback of(StudentProfile student, TeacherProfile teacher,
                              FeedbackCategory category, LocalDate date, String content,
                              boolean studentVisible, boolean parentVisible) {
        Feedback feedback = new Feedback();
        feedback.student = student;
        feedback.teacher = teacher;
        feedback.category = category;
        feedback.date = date;
        feedback.content = content;
        feedback.studentVisible = studentVisible;
        feedback.parentVisible = parentVisible;
        return feedback;
    }

    public void update(FeedbackCategory category, LocalDate date, String content,
                       boolean studentVisible, boolean parentVisible) {
        this.category = category;
        this.date = date;
        this.content = content;
        this.studentVisible = studentVisible;
        this.parentVisible = parentVisible;
    }

    public void updateVisibility(boolean studentVisible, boolean parentVisible) {
        this.studentVisible = studentVisible;
        this.parentVisible = parentVisible;
    }
}
