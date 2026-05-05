package com.example.edumanager.facade;

import com.example.edumanager.domain.attendance.dto.AttendanceResponse;
import com.example.edumanager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.edumanager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.edumanager.domain.attendance.entity.Attendance;
import com.example.edumanager.domain.attendance.service.AttendanceService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceOperationFacade {

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getList(Long studentId, int year, int month, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        return attendanceService.findByStudentAndMonth(studentId, year, month).stream()
                .map(AttendanceResponse::of)
                .toList();
    }

    @Transactional
    public AttendanceResponse create(Long studentId, CreateAttendanceRequest request, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        return AttendanceResponse.of(
                attendanceService.save(student, request, teacherService.getProfileByUserId(userDetails.getUserId()))
        );
    }

    @Transactional
    public AttendanceResponse update(Long studentId, Long attendanceId,
                                     UpdateAttendanceRequest request, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        Attendance attendance = attendanceService.getByIdAndStudentId(attendanceId, student.getId());
        return AttendanceResponse.of(attendanceService.update(attendance, request));
    }

    @Transactional
    public void delete(Long studentId, Long attendanceId, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        Attendance attendance = attendanceService.getByIdAndStudentId(attendanceId, student.getId());
        attendanceService.delete(attendance);
    }

    private void checkHomeroomAccess(Long teacherUserId, StudentProfile student, Role role) {
        if (role != Role.TEACHER) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);

        TeacherProfile teacher = teacherService.getProfileByUserId(teacherUserId);
        boolean isHomeroom = teacher.getGrade() == student.getGrade()
                && teacher.getClassNum() == student.getClassNum()
                && teacher.getSchool() == student.getSchool();

        if (!isHomeroom) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
    }
}
