package com.example.EduManager.domain.student.service;

import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.repository.ParentStudentRepository;
import com.example.EduManager.domain.student.repository.StudentProfileRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService 단위 테스트")
class StudentServiceTest {

    @Mock StudentProfileRepository studentProfileRepository;
    @Mock ParentStudentRepository parentStudentRepository;

    @InjectMocks
    StudentService studentService;

    @Mock StudentProfile student;

    @Nested
    @DisplayName("1. updateDetail() - parseBirthDate")
    class UpdateDetail {

        @Test
        @DisplayName("TC-1-1. 생년월일 null → null 전달, updateDetail 호출")
        void nullBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", null, null, null, null);

            studentService.updateDetail(student, request);

            verify(student).updateDetail(null, null, null, null);
        }

        @Test
        @DisplayName("TC-1-2. 유효한 날짜 형식 → LocalDate 변환 후 updateDetail 호출")
        void validBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", "2008-01-01", null, null, null);

            studentService.updateDetail(student, request);

            verify(student).updateDetail(LocalDate.of(2008, 1, 1), null, null, null);
        }

        @Test
        @DisplayName("TC-1-3. 잘못된 날짜 형식 → INVALID_BIRTH_DATE, updateDetail never")
        void invalidBirthDate() {
            UpdateStudentRequest request = UpdateStudentRequest.of("홍길동", "1", null, null, null);

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.updateDetail(student, request));

            assertAll(
                    () -> assertEquals(ErrorCode.INVALID_BIRTH_DATE, ex.getErrorCode()),
                    () -> verify(student, never()).updateDetail(any(), any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("2. getById()")
    class GetById {

        @Test
        @DisplayName("TC-2-1. 성공 → StudentProfile 반환")
        void success() {
            when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(student));

            assertEquals(student, studentService.getById(1L));
        }

        @Test
        @DisplayName("TC-2-2. 없음 → STUDENT_NOT_FOUND")
        void notFound() {
            when(studentProfileRepository.findById(999L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.getById(999L));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("3. getProfileByUser()")
    class GetProfileByUser {

        @Mock User user;

        @Test
        @DisplayName("TC-3-1. 성공 → StudentProfile 반환")
        void success() {
            when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(student));

            assertEquals(student, studentService.getProfileByUser(user));
        }

        @Test
        @DisplayName("TC-3-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(studentProfileRepository.findByUser(user)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> studentService.getProfileByUser(user));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
