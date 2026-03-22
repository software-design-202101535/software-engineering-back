package com.example.EduManager.domain.grade.entity;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uq_grades", columnNames = {"student_id", "semester", "subject", "exam_type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(nullable = false, length = 10)
    private String semester;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private GradeLevel grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 10)
    private ExamType examType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Grade of(StudentProfile student, String semester, String subject,
                           int score, ExamType examType, User teacher) {
        Grade grade = new Grade();
        grade.student = student;
        grade.semester = semester;
        grade.subject = subject;
        grade.score = score;
        grade.grade = GradeLevel.from(score);
        grade.examType = examType;
        grade.teacher = teacher;
        grade.createdAt = LocalDateTime.now();
        grade.updatedAt = LocalDateTime.now();
        return grade;
    }

    public void updateScore(int score) {
        this.score = score;
        this.grade = GradeLevel.from(score);
        this.updatedAt = LocalDateTime.now();
    }
}
