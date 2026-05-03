package com.example.edumanager.domain.attendance.dto;

import com.example.edumanager.domain.attendance.entity.Attendance;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private LocalDate date;
    private String status;
    private String reason;

    public static AttendanceResponse ofForTest(Long id) {
        AttendanceResponse response = new AttendanceResponse();
        response.id = id;
        return response;
    }

    public static AttendanceResponse of(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.id = attendance.getId();
        response.studentId = attendance.getStudent().getId();
        response.date = attendance.getDate();
        response.status = attendance.getStatus().name();
        response.reason = attendance.getNote();
        return response;
    }
}
