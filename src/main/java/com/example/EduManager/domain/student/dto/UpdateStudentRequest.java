package com.example.EduManager.domain.student.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateStudentRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private LocalDate birthDate;
    private String phone;
    private String parentPhone;
    private String address;

    public static UpdateStudentRequest of(String name, LocalDate birthDate,
                                          String phone, String parentPhone, String address) {
        UpdateStudentRequest request = new UpdateStudentRequest();
        request.name = name;
        request.birthDate = birthDate;
        request.phone = phone;
        request.parentPhone = parentPhone;
        request.address = address;
        return request;
    }
}
