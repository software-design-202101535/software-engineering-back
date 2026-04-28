package com.example.EduManager.domain.teacher.service;

import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.domain.teacher.repository.TeacherProfileRepository;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService 단위 테스트")
class TeacherServiceTest {

    @Mock TeacherProfileRepository teacherProfileRepository;

    @InjectMocks
    TeacherService teacherService;

    @Mock User user;
    @Mock TeacherProfile profile;

    @Nested
    @DisplayName("1. createProfile()")
    class CreateProfile {

        @Test
        @DisplayName("TC-1-1. 성공 → save 호출, 반환값")
        void success() {
            when(teacherProfileRepository.save(any())).thenReturn(profile);

            TeacherProfile result = teacherService.createProfile(user, School.SUNRIN_HIGH_SCHOOL, 2, 3);

            assertAll(
                    () -> verify(teacherProfileRepository).save(any()),
                    () -> assertEquals(profile, result)
            );
        }
    }

    @Nested
    @DisplayName("2. getProfileByUser()")
    class GetProfileByUser {

        @Test
        @DisplayName("TC-2-1. 성공 → TeacherProfile 반환")
        void success() {
            when(teacherProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

            assertEquals(profile, teacherService.getProfileByUser(user));
        }

        @Test
        @DisplayName("TC-2-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(teacherProfileRepository.findByUser(user)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> teacherService.getProfileByUser(user));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("3. getProfileByUserId()")
    class GetProfileByUserId {

        @Test
        @DisplayName("TC-3-1. 성공 → TeacherProfile 반환")
        void success() {
            when(teacherProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

            assertEquals(profile, teacherService.getProfileByUserId(10L));
        }

        @Test
        @DisplayName("TC-3-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(teacherProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> teacherService.getProfileByUserId(10L));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
