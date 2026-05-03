package com.example.edumanager.domain.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateStudentRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private String birthDate;

    @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phone;

    @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String parentPhone;
    private String address;

    public static UpdateStudentRequest of(String name, String birthDate,
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
