package com.example.EduManager.facade;

import com.example.EduManager.domain.student.dto.StudentDetailResponse;
import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
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
public class StudentOperationFacade {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<StudentSummaryResponse> getClassStudents(UserDetailsImpl userDetails) {
        if (userDetails.getRole() != Role.TEACHER) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
        TeacherProfile teacher = teacherService.getProfileByUserId(userDetails.getUserId());
        return toSummaryResponses(studentService.getClassStudents(teacher.getGrade(), teacher.getClassNum(), teacher.getUser().getSchool()));
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudentDetail(Long studentId, UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        return StudentDetailResponse.of(student);
    }

    @Transactional
    public StudentDetailResponse updateStudentDetail(Long studentId, UpdateStudentRequest request,
                                                     UserDetailsImpl userDetails) {
        StudentProfile student = studentService.getById(studentId);
        checkHomeroomAccess(userDetails.getUserId(), student, userDetails.getRole());
        userService.updateName(student.getUser(), request.getName());
        studentService.updateDetail(student, request);
        return StudentDetailResponse.of(student);
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

    private List<StudentSummaryResponse> toSummaryResponses(List<StudentProfile> students) {
        return students.stream().map(StudentSummaryResponse::of).toList();
    }
}
