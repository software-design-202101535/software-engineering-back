package com.example.EduManager.domain.counseling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateCounselingRequest {

    @NotNull(message = "상담 날짜를 입력해주세요.")
    private LocalDate counselingDate;

    @NotBlank(message = "상담 내용을 입력해주세요.")
    private String content;

    private String nextPlan;

    private LocalDate nextDate;

    private boolean sharedWithTeachers;

    public static UpdateCounselingRequest of(LocalDate counselingDate, String content,
                                             String nextPlan, LocalDate nextDate, boolean sharedWithTeachers) {
        UpdateCounselingRequest request = new UpdateCounselingRequest();
        request.counselingDate = counselingDate;
        request.content = content;
        request.nextPlan = nextPlan;
        request.nextDate = nextDate;
        request.sharedWithTeachers = sharedWithTeachers;
        return request;
    }
}
