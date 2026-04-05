package com.example.EduManager.domain.user.service;

import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.repository.UserRepository;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerSchoolUser(String email, String rawPassword, String name, Role role, School school, String schoolNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATED_USER);
        }
        return userRepository.save(User.of(email, passwordEncoder.encode(rawPassword), name, role, school, schoolNumber));
    }

    public User registerParentUser(String email, String rawPassword, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATED_USER);
        }
        return userRepository.save(User.ofParent(email, passwordEncoder.encode(rawPassword), name));
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_CREDENTIALS));
    }

    public User getBySchoolAndSchoolNumber(School school, String schoolNumber) {
        return userRepository.findBySchoolAndSchoolNumber(school, schoolNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_CREDENTIALS));
    }

    public User getStudentBySchoolAndSchoolNumber(School school, String schoolNumber) {
        return userRepository.findBySchoolAndSchoolNumber(school, schoolNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void updateName(User user, String name) {
        user.updateName(name);
    }
}
