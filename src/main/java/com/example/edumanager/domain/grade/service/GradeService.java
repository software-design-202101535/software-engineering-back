package com.example.edumanager.domain.grade.service;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.grade.entity.Grade;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.grade.repository.GradeRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    public List<Grade> getGrades(StudentProfile student, String semester, ExamType examType) {
        return gradeRepository.findAllByStudentAndSemesterAndExamType(student, semester, examType);
    }

    public List<Grade> batchProcess(StudentProfile student, BatchGradeRequest request) {
        List<Grade> toUpdate = findGradesByIds(request.getUpdate().stream().map(BatchGradeRequest.UpdateItem::getId).toList());
        List<Grade> toDelete = findGradesByIds(request.getDelete());

        gradeRepository.deleteAll(toDelete);
        applyUpdates(student, request.getSemester(), request.getExamType(), toUpdate, request.getUpdate());
        applyCreates(student, request.getSemester(), request.getExamType(), request.getCreate());

        return gradeRepository.findAllByStudentAndSemesterAndExamType(student, request.getSemester(), request.getExamType());
    }

    private List<Grade> findGradesByIds(List<Long> ids) {
        List<Grade> grades = gradeRepository.findAllById(ids);
        if (grades.size() != ids.size()) {
            throw new CustomException(ErrorCode.GRADE_NOT_FOUND);
        }
        return grades;
    }

    private void applyCreates(StudentProfile student, String semester, ExamType examType,
                              List<BatchGradeRequest.CreateItem> items) {
        if (items.isEmpty()) return;

        List<Subject> subjects = items.stream().map(BatchGradeRequest.CreateItem::getSubject).toList();
        if (!gradeRepository.findExistingSubjects(student, semester, examType, subjects).isEmpty()) {
            throw new CustomException(ErrorCode.GRADE_ALREADY_EXISTS);
        }
        gradeRepository.saveAll(items.stream()
                .map(item -> Grade.of(student, semester, item.getSubject(), item.getScore(), examType))
                .toList());
    }

    private void applyUpdates(StudentProfile student, String semester, ExamType examType,
                              List<Grade> grades, List<BatchGradeRequest.UpdateItem> items) {
        List<Subject> newSubjects = items.stream().map(BatchGradeRequest.UpdateItem::getSubject).toList();
        List<Long> updateIds = grades.stream().map(Grade::getId).toList();

        if (!gradeRepository.findConflictingSubjects(student, semester, examType, newSubjects, updateIds).isEmpty()) {
            throw new CustomException(ErrorCode.GRADE_ALREADY_EXISTS);
        }

        for (int i = 0; i < grades.size(); i++) {
            grades.get(i).update(items.get(i).getSubject(), items.get(i).getScore());
        }
    }

}
