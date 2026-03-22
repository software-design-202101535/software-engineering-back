package com.example.EduManager.domain.counseling.entity;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "counselings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counseling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String nextPlan;

    private LocalDate nextDate;

    @Column(nullable = false)
    private boolean isSharedWithTeachers;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Counseling of(StudentProfile student, User teacher, LocalDate date, String content, String nextPlan, LocalDate nextDate) {
        Counseling counseling = new Counseling();
        counseling.student = student;
        counseling.teacher = teacher;
        counseling.date = date;
        counseling.content = content;
        counseling.nextPlan = nextPlan;
        counseling.nextDate = nextDate;
        counseling.isSharedWithTeachers = false;
        counseling.createdAt = LocalDateTime.now();
        counseling.updatedAt = LocalDateTime.now();
        return counseling;
    }

    public void update(String content, String nextPlan, LocalDate nextDate) {
        this.content = content;
        this.nextPlan = nextPlan;
        this.nextDate = nextDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void shareWithTeachers() {
        this.isSharedWithTeachers = true;
        this.updatedAt = LocalDateTime.now();
    }
}
