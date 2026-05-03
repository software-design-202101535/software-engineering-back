package com.example.edumanager.domain.attendance.dto;

import com.example.edumanager.domain.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateAttendanceRequest {

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;

    @NotNull(message = "출결 상태를 입력해주세요.")
    private AttendanceStatus status;

    private String reason;

    public static CreateAttendanceRequest of(LocalDate date, AttendanceStatus status, String reason) {
        CreateAttendanceRequest request = new CreateAttendanceRequest();
        request.date = date;
        request.status = status;
        request.reason = reason;
        return request;
    }
}
