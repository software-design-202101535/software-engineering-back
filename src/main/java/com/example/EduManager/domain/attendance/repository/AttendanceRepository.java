package com.example.EduManager.domain.attendance.repository;

import com.example.EduManager.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
            "AND YEAR(a.date) = :year AND MONTH(a.date) = :month " +
            "ORDER BY a.date ASC")
    List<Attendance> findByStudentIdAndYearAndMonth(@Param("studentId") Long studentId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);

    Optional<Attendance> findByIdAndStudentId(Long id, Long studentId);
}
