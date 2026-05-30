package com.example.edumanager.domain.counseling.repository;

import com.example.edumanager.domain.counseling.entity.Counseling;
import com.example.edumanager.domain.user.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CounselingRepository extends JpaRepository<Counseling, Long> {

    @Query("SELECT c FROM Counseling c " +
           "JOIN FETCH c.student s JOIN FETCH s.user su " +
           "JOIN FETCH c.teacher t JOIN FETCH t.user tu " +
           "WHERE c.isSharedWithTeachers = true " +
           "AND s.school = :school " +
           "AND tu.id <> :requesterUserId " +
           "AND YEAR(c.date) = :year " +
           "AND (:month IS NULL OR MONTH(c.date) = :month) " +
           "AND (:grade IS NULL OR s.grade = :grade) " +
           "AND (:classNum IS NULL OR s.classNum = :classNum) " +
           "AND (:name IS NULL OR su.name LIKE CONCAT('%', :name, '%')) " +
           "ORDER BY c.date DESC, c.id DESC")
    List<Counseling> findSharedForTeacher(@Param("school") School school,
                                          @Param("requesterUserId") Long requesterUserId,
                                          @Param("year") int year,
                                          @Param("month") Integer month,
                                          @Param("grade") Integer grade,
                                          @Param("classNum") Integer classNum,
                                          @Param("name") String name);

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher t JOIN FETCH t.user " +
           "WHERE c.student.id = :studentId " +
           "AND (t.user.id = :teacherId OR c.isSharedWithTeachers = true) " +
           "AND YEAR(c.date) = :year")
    List<Counseling> findByStudentForTeacherByYear(@Param("studentId") Long studentId,
                                                   @Param("teacherId") Long teacherId,
                                                   @Param("year") int year);

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher t JOIN FETCH t.user " +
           "WHERE c.student.id = :studentId " +
           "AND (t.user.id = :teacherId OR c.isSharedWithTeachers = true) " +
           "AND YEAR(c.date) = :year AND MONTH(c.date) = :month")
    List<Counseling> findByStudentForTeacherByYearAndMonth(@Param("studentId") Long studentId,
                                                           @Param("teacherId") Long teacherId,
                                                           @Param("year") int year,
                                                           @Param("month") int month);

    @Query("SELECT c FROM Counseling c JOIN FETCH c.teacher t JOIN FETCH t.user " +
           "WHERE c.id = :id AND c.student.id = :studentId")
    Optional<Counseling> findByIdAndStudentId(@Param("id") Long id, @Param("studentId") Long studentId);
}
