package com.example.EduManager.domain.student.repository;

import com.example.EduManager.domain.student.entity.ParentStudent;
import com.example.EduManager.domain.student.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    boolean existsByStudent(StudentProfile student);
    List<ParentStudent> findAllByStudent(StudentProfile student);
}
