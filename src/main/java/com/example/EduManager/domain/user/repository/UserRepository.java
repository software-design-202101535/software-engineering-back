package com.example.EduManager.domain.user.repository;

import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findBySchoolAndSchoolNumber(School school, String schoolNumber);
    boolean existsByEmail(String email);
}
