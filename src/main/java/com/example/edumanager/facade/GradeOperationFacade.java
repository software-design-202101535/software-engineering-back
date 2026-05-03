package com.example.edumanager.facade;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.dto.GradeResponse;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.grade.entity.Grade;
import com.example.edumanager.domain.grade.service.GradeService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.service.TeacherService;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GradeOperationFacade {

    private final GradeService gradeService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Transactional(readOnly = true)
    public List<GradeResponse> getGrades(Long studentId, UserDetailsImpl userDetails,
                                         String semester, ExamType examType) {
        StudentProfile student = studentService.getById(studentId);
        checkReadAccess(student, userDetails);
        return gradeService.getGrades(student, semester, examType).stream()
                .map(GradeResponse::of)
                .toList();
    }

    @Transactional
    public List<GradeResponse> batchProcess(Long studentId, UserDetailsImpl userDetails,
                                            BatchGradeRequest request) {
        StudentProfile student = studentService.getById(studentId);
        checkWriteAccess(userDetails.getUserId(), student, userDetails.getRole());
        List<Grade> grades = gradeService.batchProcess(student, request);
        return grades.stream().map(GradeResponse::of).toList();
    }

    private void checkReadAccess(StudentProfile student, UserDetailsImpl userDetails) {
        Role role = userDetails.getRole();

        if (role == Role.ADMIN) return;
        if (role == Role.TEACHER) { checkHomeroomAccess(userDetails.getUserId(), student); return; }
        if (role == Role.STUDENT) { checkStudentAccess(userDetails.getUserId(), student); return; }
        if (role == Role.PARENT)  { checkParentAccess(userDetails.getUserId(), student); return; }

        throw new CustomException(ErrorCode.GRADE_ACCESS_DENIED);
    }

    private void checkStudentAccess(Long userId, StudentProfile student) {
        if (!student.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.GRADE_ACCESS_DENIED);
        }
    }

    private void checkParentAccess(Long userId, StudentProfile student) {
        List<User> parents = studentService.getParentsByStudentId(student.getId());
        boolean isLinked = parents.stream().anyMatch(parent -> parent.getId().equals(userId));
        if (!isLinked) {
            throw new CustomException(ErrorCode.GRADE_ACCESS_DENIED);
        }
    }

    private void checkWriteAccess(Long teacherUserId, StudentProfile student, Role role) {
        if (role == Role.ADMIN) return;
        checkHomeroomAccess(teacherUserId, student);
    }

    private void checkHomeroomAccess(Long teacherUserId, StudentProfile student) {
        TeacherProfile teacherProfile = teacherService.getProfileByUserId(teacherUserId);
        boolean isHomeroom = teacherProfile.getGrade() == student.getGrade()
                && teacherProfile.getClassNum() == student.getClassNum()
                && teacherProfile.getSchool() == student.getSchool();

        if (!isHomeroom) {
            throw new CustomException(ErrorCode.GRADE_ACCESS_DENIED);
        }
    }
}
