package com.example.EduManager.domain.counseling.service;

import com.example.EduManager.domain.counseling.entity.Counseling;
import com.example.EduManager.domain.counseling.repository.CounselingRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CounselingService 단위 테스트")
class CounselingServiceTest {

    @Mock
    CounselingRepository counselingRepository;

    @InjectMocks
    CounselingService counselingService;

    @Nested
    @DisplayName("1. getByIdAndStudentId()")
    class GetByIdAndStudentId {

        @Test
        @DisplayName("TC-1-1. 존재하지 않음 → COUNSELING_NOT_FOUND")
        void notFound() {
            when(counselingRepository.findByIdAndStudentId(999L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> counselingService.getByIdAndStudentId(999L, 1L));

            assertEquals(ErrorCode.COUNSELING_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-1-2. 성공 → Counseling 반환")
        void success() {
            Counseling counseling = mock(Counseling.class);
            when(counselingRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(counseling));

            assertEquals(counseling, counselingService.getByIdAndStudentId(1L, 1L));
        }
    }
}
