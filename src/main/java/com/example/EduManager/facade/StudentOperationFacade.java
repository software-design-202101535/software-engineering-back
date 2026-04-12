package com.example.EduManager.facade;

import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
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

    @Transactional(readOnly = true)
    public List<StudentSummaryResponse> getClassStudents(UserDetailsImpl userDetails) {
        if (userDetails.getRole() != Role.TEACHER) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
        TeacherProfile teacher = teacherService.getProfileByUserId(userDetails.getUserId());
        return toSummaryResponses(studentService.getClassStudents(teacher.getGrade(), teacher.getClassNum(), teacher.getUser().getSchool()));
    }

    private List<StudentSummaryResponse> toSummaryResponses(List<StudentProfile> students) {
        return students.stream().map(StudentSummaryResponse::of).toList();
    }
}
