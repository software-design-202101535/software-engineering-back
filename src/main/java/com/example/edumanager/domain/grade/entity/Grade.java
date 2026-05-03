package com.example.edumanager.domain.grade.entity;

import com.example.edumanager.domain.student.entity.StudentProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uq_grades", columnNames = {"student_id", "semester", "subject", "exam_type"}))
@EntityListeners(AuditingEntityListener.class)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Subject subject;

    @Column
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private GradeLevel grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 10)
    private ExamType examType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Grade of(StudentProfile student, String semester, Subject subject,
                           Integer score, ExamType examType) {
        Grade grade = new Grade();
        grade.student = student;
        grade.semester = semester;
        grade.subject = subject;
        grade.score = score;
        grade.grade = score != null ? GradeLevel.from(score) : null;
        grade.examType = examType;
        return grade;
    }

    public void update(Subject subject, Integer score) {
        this.subject = subject;
        this.score = score;
        this.grade = score != null ? GradeLevel.from(score) : null;
    }
}
