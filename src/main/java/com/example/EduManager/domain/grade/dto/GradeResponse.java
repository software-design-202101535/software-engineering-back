package com.example.EduManager.domain.grade.dto;

import com.example.EduManager.domain.grade.entity.Grade;
import lombok.Getter;

@Getter
public class GradeResponse {

    private final Long id;
    private final String subject;
    private final Integer score;
    private final String grade;

    private GradeResponse(Grade grade) {
        this.id = grade.getId();
        this.subject = grade.getSubject().name();
        this.score = grade.getScore();
        this.grade = grade.getGrade() != null ? grade.getGrade().name() : null;
    }

    public static GradeResponse of(Grade grade) {
        return new GradeResponse(grade);
    }
}
