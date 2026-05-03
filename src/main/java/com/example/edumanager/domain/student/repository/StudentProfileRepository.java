package com.example.edumanager.domain.student.repository;

import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);

    @Query("SELECT s FROM StudentProfile s WHERE s.grade = :grade AND s.classNum = :classNum AND s.school = :school ORDER BY s.number ASC")
    List<StudentProfile> findAllByGradeAndClassNumAndSchool(@Param("grade") int grade, @Param("classNum") int classNum, @Param("school") School school);
}
