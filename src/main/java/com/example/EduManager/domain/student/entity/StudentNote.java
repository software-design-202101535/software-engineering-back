package com.example.EduManager.domain.student.entity;

import com.example.EduManager.domain.user.entity.User;
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
@Table(name = "student_notes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static StudentNote of(StudentProfile student, NoteCategory category,
                                 String content, LocalDate date, User teacher) {
        StudentNote note = new StudentNote();
        note.student = student;
        note.category = category;
        note.content = content;
        note.date = date;
        note.teacher = teacher;
        return note;
    }

    public void update(NoteCategory category, String content, LocalDate date) {
        this.category = category;
        this.content = content;
        this.date = date;
    }
}
