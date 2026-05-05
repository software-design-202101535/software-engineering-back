package com.example.edumanager.domain.counseling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateCounselingRequest {

    @NotNull(message = "상담 날짜를 입력해주세요.")
    private LocalDate counselingDate;

    @NotBlank(message = "상담 내용을 입력해주세요.")
    private String content;

    private String nextPlan;

    private LocalDate nextDate;

    private boolean sharedWithTeachers;

    public static CreateCounselingRequest of(LocalDate counselingDate, String content,
                                             String nextPlan, LocalDate nextDate, boolean sharedWithTeachers) {
        CreateCounselingRequest request = new CreateCounselingRequest();
        request.counselingDate = counselingDate;
        request.content = content;
        request.nextPlan = nextPlan;
        request.nextDate = nextDate;
        request.sharedWithTeachers = sharedWithTeachers;
        return request;
    }
}
