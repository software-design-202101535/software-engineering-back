package com.example.EduManager.facade;

import com.example.EduManager.domain.counseling.dto.CounselingResponse;
import com.example.EduManager.domain.counseling.dto.CreateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.EduManager.domain.counseling.entity.Counseling;
import com.example.EduManager.domain.counseling.service.CounselingService;
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
public class CounselingOperationFacade {

    private final CounselingService counselingService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Transactional(readOnly = true)
    public List<CounselingResponse> getList(Long studentId, int year, Integer month, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        return fetchCounselings(studentId, userDetails.getUserId(), year, month).stream()
                .map(CounselingResponse::of).toList();
    }

    @Transactional
    public CounselingResponse create(Long studentId, CreateCounselingRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        StudentProfile student = studentService.getById(studentId);
        TeacherProfile teacher = teacherService.getProfileByUserId(userDetails.getUserId());
        return CounselingResponse.of(counselingService.save(student, teacher, request));
    }

    @Transactional
    public CounselingResponse update(Long studentId, Long counselingId, UpdateCounselingRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Counseling counseling = counselingService.getByIdAndStudentId(counselingId, studentId);
        checkAuthor(counseling, userDetails);
        return CounselingResponse.of(counselingService.update(counseling, request));
    }

    @Transactional
    public CounselingResponse updateShare(Long studentId, Long counselingId, UpdateCounselingShareRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Counseling counseling = counselingService.getByIdAndStudentId(counselingId, studentId);
        checkAuthor(counseling, userDetails);
        return CounselingResponse.of(counselingService.updateSharedStatus(counseling, request));
    }

    @Transactional
    public void delete(Long studentId, Long counselingId, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Counseling counseling = counselingService.getByIdAndStudentId(counselingId, studentId);
        checkAuthor(counseling, userDetails);
        counselingService.delete(counseling);
    }

    private List<Counseling> fetchCounselings(Long studentId, Long teacherId, int year, Integer month) {
        return month == null
                ? counselingService.findForTeacherByYear(studentId, teacherId, year)
                : counselingService.findForTeacherByYearAndMonth(studentId, teacherId, year, month);
    }

    private void checkTeacherOrAdmin(Role role) {
        if (role != Role.TEACHER && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
    }

    private void checkAuthor(Counseling counseling, UserDetailsImpl userDetails) {
        if (userDetails.getRole() == Role.ADMIN) return;
        if (!counseling.getTeacher().getUser().getId().equals(userDetails.getUserId())) {
            throw new CustomException(ErrorCode.COUNSELING_ACCESS_DENIED);
        }
    }
}
