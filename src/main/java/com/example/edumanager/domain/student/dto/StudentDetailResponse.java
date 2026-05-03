package com.example.edumanager.domain.student.dto;

import com.example.edumanager.domain.student.entity.StudentProfile;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class StudentDetailResponse {

    private Long id;
    private String name;
    private int grade;
    private int classNum;
    private int number;
    private LocalDate birthDate;
    private String phone;
    private String parentPhone;
    private String address;

    public static StudentDetailResponse ofForTest(Long id, String name) {
        StudentDetailResponse response = new StudentDetailResponse();
        response.id = id;
        response.name = name;
        return response;
    }

    public static StudentDetailResponse of(StudentProfile profile) {
        StudentDetailResponse response = new StudentDetailResponse();
        response.id = profile.getId();
        response.name = profile.getUser().getName();
        response.grade = profile.getGrade();
        response.classNum = profile.getClassNum();
        response.number = profile.getNumber();
        response.birthDate = profile.getBirthDate();
        response.phone = profile.getPhone();
        response.parentPhone = profile.getParentPhone();
        response.address = profile.getAddress();
        return response;
    }
}
