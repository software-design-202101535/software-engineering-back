package com.example.EduManager.domain.teacher.service;

import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.repository.TeacherProfileRepository;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherProfileRepository teacherProfileRepository;

    public TeacherProfile createProfile(User user, int grade, int classNum) {
        return teacherProfileRepository.save(TeacherProfile.of(user, grade, classNum));
    }

    public TeacherProfile getProfileByUser(User user) {
        return teacherProfileRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public TeacherProfile getProfileByUserId(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
