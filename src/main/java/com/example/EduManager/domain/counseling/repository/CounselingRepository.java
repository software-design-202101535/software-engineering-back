package com.example.EduManager.domain.counseling.repository;

import com.example.EduManager.domain.counseling.entity.Counseling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CounselingRepository extends JpaRepository<Counseling, Long> {

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher " +
           "WHERE c.student.id = :studentId " +
           "AND (c.teacher.id = :teacherId OR c.isSharedWithTeachers = true) " +
           "AND YEAR(c.date) = :year")
    List<Counseling> findByStudentForTeacherByYear(@Param("studentId") Long studentId,
                                                   @Param("teacherId") Long teacherId,
                                                   @Param("year") int year);

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher " +
           "WHERE c.student.id = :studentId " +
           "AND (c.teacher.id = :teacherId OR c.isSharedWithTeachers = true) " +
           "AND YEAR(c.date) = :year AND MONTH(c.date) = :month")
    List<Counseling> findByStudentForTeacherByYearAndMonth(@Param("studentId") Long studentId,
                                                           @Param("teacherId") Long teacherId,
                                                           @Param("year") int year,
                                                           @Param("month") int month);

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher " +
           "WHERE c.id = :id AND c.student.id = :studentId")
    Optional<Counseling> findByIdAndStudentId(@Param("id") Long id, @Param("studentId") Long studentId);
}
