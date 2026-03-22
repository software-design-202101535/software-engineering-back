package com.example.EduManager.domain.student.entity;

import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static StudentNote of(StudentProfile student, String content, User teacher) {
        StudentNote note = new StudentNote();
        note.student = student;
        note.content = content;
        note.teacher = teacher;
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();
        return note;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
