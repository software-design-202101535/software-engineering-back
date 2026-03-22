package com.example.EduManager.domain.student.entity;

import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parent_student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudent {

    @EmbeddedId
    private ParentStudentId id;

    @MapsId("parentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    public static ParentStudent of(User parent, StudentProfile student) {
        ParentStudent ps = new ParentStudent();
        ps.id = ParentStudentId.of(parent.getId(), student.getId());
        ps.parent = parent;
        ps.student = student;
        return ps;
    }
}
