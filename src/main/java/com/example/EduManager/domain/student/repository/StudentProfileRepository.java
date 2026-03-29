package com.example.EduManager.domain.student.repository;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser(User user);

    @Query("SELECT s FROM StudentProfile s JOIN FETCH s.user WHERE s.grade = :grade AND s.classNum = :classNum AND s.user.school = :school ORDER BY s.number ASC")
    List<StudentProfile> findAllByGradeAndClassNumAndUserSchool(@Param("grade") int grade, @Param("classNum") int classNum, @Param("school") School school);
}
