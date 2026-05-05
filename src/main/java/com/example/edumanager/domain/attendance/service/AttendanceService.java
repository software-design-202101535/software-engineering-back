package com.example.edumanager.domain.attendance.service;

import com.example.edumanager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.edumanager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.edumanager.domain.attendance.entity.Attendance;
import com.example.edumanager.domain.attendance.repository.AttendanceRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public List<Attendance> findByStudentAndMonth(Long studentId, int year, int month) {
        return attendanceRepository.findByStudentIdAndYearAndMonth(studentId, year, month);
    }

    public Attendance save(StudentProfile student, CreateAttendanceRequest request, TeacherProfile createdBy) {
        return attendanceRepository.save(
                Attendance.of(student, request.getDate(), request.getStatus(), request.getReason(), createdBy)
        );
    }

    public Attendance update(Attendance attendance, UpdateAttendanceRequest request) {
        attendance.update(request.getDate(), request.getStatus(), request.getReason());
        return attendance;
    }

    public void delete(Attendance attendance) {
        attendanceRepository.delete(attendance);
    }

    public Attendance getByIdAndStudentId(Long attendanceId, Long studentId) {
        return attendanceRepository.findByIdAndStudentId(attendanceId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }
}
