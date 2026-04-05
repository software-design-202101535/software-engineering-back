package com.example.EduManager.domain.attendance.dto;

import com.example.EduManager.domain.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateAttendanceRequest {

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;

    @NotNull(message = "출결 상태를 입력해주세요.")
    private AttendanceStatus status;

    private String reason;

    public static UpdateAttendanceRequest of(LocalDate date, AttendanceStatus status, String reason) {
        UpdateAttendanceRequest request = new UpdateAttendanceRequest();
        request.date = date;
        request.status = status;
        request.reason = reason;
        return request;
    }
}
