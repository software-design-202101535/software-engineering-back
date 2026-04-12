package com.example.EduManager.domain.teacher.repository;

import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserId(Long userId);
}
