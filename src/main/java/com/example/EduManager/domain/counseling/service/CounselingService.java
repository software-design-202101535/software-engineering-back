package com.example.EduManager.domain.counseling.service;

import com.example.EduManager.domain.counseling.dto.CreateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.EduManager.domain.counseling.entity.Counseling;
import com.example.EduManager.domain.counseling.repository.CounselingRepository;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselingService {

    private final CounselingRepository counselingRepository;

    public List<Counseling> findForTeacherByYear(Long studentId, Long teacherId, int year) {
        return counselingRepository.findByStudentForTeacherByYear(studentId, teacherId, year);
    }

    public List<Counseling> findForTeacherByYearAndMonth(Long studentId, Long teacherId, int year, int month) {
        return counselingRepository.findByStudentForTeacherByYearAndMonth(studentId, teacherId, year, month);
    }

    public Counseling save(StudentProfile student, TeacherProfile teacher, CreateCounselingRequest request) {
        return counselingRepository.save(
                Counseling.of(student, teacher, request.getCounselingDate(), request.getContent(),
                        request.getNextPlan(), request.getNextDate(), request.isSharedWithTeachers())
        );
    }

    public Counseling getByIdAndStudentId(Long counselingId, Long studentId) {
        return counselingRepository.findByIdAndStudentId(counselingId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUNSELING_NOT_FOUND));
    }

    public Counseling update(Counseling counseling, UpdateCounselingRequest request) {
        counseling.update(request.getContent(), request.getNextPlan(), request.getNextDate());
        return counseling;
    }

    public Counseling updateSharedStatus(Counseling counseling, UpdateCounselingShareRequest request) {
        counseling.updateSharedStatus(request.isSharedWithTeachers());
        return counseling;
    }

    public void delete(Counseling counseling) {
        counselingRepository.delete(counseling);
    }
}
