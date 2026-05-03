package com.example.edumanager.domain.student.entity;

import com.example.edumanager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parent_student",
        uniqueConstraints = @UniqueConstraint(name = "uq_parent_student", columnNames = {"parent_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    public static ParentStudent of(User parent, StudentProfile student) {
        ParentStudent ps = new ParentStudent();
        ps.parent = parent;
        ps.student = student;
        return ps;
    }
}
