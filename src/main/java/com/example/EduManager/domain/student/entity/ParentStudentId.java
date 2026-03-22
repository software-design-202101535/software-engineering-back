package com.example.EduManager.domain.student.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudentId implements Serializable {

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "student_id")
    private Long studentId;

    public static ParentStudentId of(Long parentId, Long studentId) {
        ParentStudentId id = new ParentStudentId();
        id.parentId = parentId;
        id.studentId = studentId;
        return id;
    }
}
