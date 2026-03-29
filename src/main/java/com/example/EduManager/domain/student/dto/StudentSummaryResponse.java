package com.example.EduManager.domain.student.dto;

import com.example.EduManager.domain.student.entity.StudentProfile;
import lombok.Getter;

@Getter
public class StudentSummaryResponse {

    private Long id;
    private String name;
    private int grade;
    private int classNum;
    private int number;

    public static StudentSummaryResponse of(StudentProfile profile) {
        StudentSummaryResponse response = new StudentSummaryResponse();
        response.id = profile.getId();
        response.name = profile.getUser().getName();
        response.grade = profile.getGrade();
        response.classNum = profile.getClassNum();
        response.number = profile.getNumber();
        return response;
    }
}
