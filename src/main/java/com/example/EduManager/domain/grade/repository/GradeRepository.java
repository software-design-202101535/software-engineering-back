package com.example.EduManager.domain.grade.repository;

import com.example.EduManager.domain.grade.entity.ExamType;
import com.example.EduManager.domain.grade.entity.Grade;
import com.example.EduManager.domain.grade.entity.Subject;
import com.example.EduManager.domain.student.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g.subject FROM Grade g WHERE g.student = :student AND g.semester = :semester AND g.examType = :examType AND g.subject IN :subjects AND g.id NOT IN :excludeIds")
    List<Subject> findConflictingSubjects(@Param("student") StudentProfile student, @Param("semester") String semester,
                                          @Param("examType") ExamType examType, @Param("subjects") List<Subject> subjects,
                                          @Param("excludeIds") List<Long> excludeIds);

    @Query("SELECT g.subject FROM Grade g WHERE g.student = :student AND g.semester = :semester AND g.examType = :examType AND g.subject IN :subjects")
    List<Subject> findExistingSubjects(@Param("student") StudentProfile student, @Param("semester") String semester,
                                       @Param("examType") ExamType examType, @Param("subjects") List<Subject> subjects);

    List<Grade> findAllByStudentAndSemesterAndExamType(StudentProfile student, String semester, ExamType examType);
}
