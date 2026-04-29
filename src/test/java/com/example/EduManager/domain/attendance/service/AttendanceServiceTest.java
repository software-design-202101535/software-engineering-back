package com.example.EduManager.domain.attendance.service;

import com.example.EduManager.domain.attendance.entity.Attendance;
import com.example.EduManager.domain.attendance.repository.AttendanceRepository;
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
@DisplayName("AttendanceService 단위 테스트")
class AttendanceServiceTest {

    @Mock
    AttendanceRepository attendanceRepository;

    @InjectMocks
    AttendanceService attendanceService;

    @Nested
    @DisplayName("1. getByIdAndStudentId()")
    class GetByIdAndStudentId {

        @Test
        @DisplayName("TC-1-1. 존재하지 않음 → ATTENDANCE_NOT_FOUND")
        void notFound() {
            when(attendanceRepository.findByIdAndStudentId(999L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> attendanceService.getByIdAndStudentId(999L, 1L));

            assertEquals(ErrorCode.ATTENDANCE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-1-2. 성공 → Attendance 반환")
        void success() {
            Attendance attendance = mock(Attendance.class);
            when(attendanceRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(attendance));

            assertEquals(attendance, attendanceService.getByIdAndStudentId(1L, 1L));
        }
    }
}
