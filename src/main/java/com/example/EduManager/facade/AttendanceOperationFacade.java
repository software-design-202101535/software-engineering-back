package com.example.EduManager.facade;

import com.example.EduManager.domain.attendance.dto.AttendanceResponse;
import com.example.EduManager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.EduManager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.EduManager.domain.attendance.entity.Attendance;
import com.example.EduManager.domain.attendance.service.AttendanceService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.service.UserService;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.UserDetailsImpl;
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
    private final UserService userService;

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
                attendanceService.save(student, request, userService.getById(userDetails.getUserId()))
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
        if (role == Role.ADMIN) return;
        if (role != Role.TEACHER) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);

        TeacherProfile teacher = teacherService.getProfileByUserId(teacherUserId);
        boolean isHomeroom = teacher.getGrade() == student.getGrade()
                && teacher.getClassNum() == student.getClassNum()
                && teacher.getUser().getSchool() == student.getUser().getSchool();

        if (!isHomeroom) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
    }
}
