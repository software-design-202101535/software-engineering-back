package com.example.EduManager.domain.student.service;

import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.repository.ParentStudentRepository;
import com.example.EduManager.domain.student.repository.StudentProfileRepository;
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
}
