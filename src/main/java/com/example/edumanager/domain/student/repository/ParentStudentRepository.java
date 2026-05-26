package com.example.edumanager.domain.student.repository;

import com.example.edumanager.domain.student.entity.ParentStudent;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    boolean existsByStudent(StudentProfile student);
    List<ParentStudent> findAllByStudent(StudentProfile student);
    List<ParentStudent> findAllByParent(User parent);

    @Query("SELECT p FROM ParentStudent ps JOIN ps.parent p WHERE ps.student.id = :studentId")
    List<User> findAllParentUsersByStudentId(@Param("studentId") Long studentId);
}
