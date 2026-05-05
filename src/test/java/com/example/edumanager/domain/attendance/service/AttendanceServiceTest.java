package com.example.edumanager.domain.attendance.service;

import com.example.edumanager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.edumanager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.edumanager.domain.attendance.entity.Attendance;
import com.example.edumanager.domain.attendance.entity.AttendanceStatus;
import com.example.edumanager.domain.attendance.repository.AttendanceRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService 단위 테스트")
class AttendanceServiceTest {

    @Mock AttendanceRepository attendanceRepository;

    @InjectMocks
    AttendanceService attendanceService;

    @Mock StudentProfile student;
    @Mock TeacherProfile teacher;

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

    @Nested
    @DisplayName("2. findByStudentAndMonth()")
    class FindByStudentAndMonth {

        @Test
        @DisplayName("TC-2-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Attendance attendance = mock(Attendance.class);
            when(attendanceRepository.findByStudentIdAndYearAndMonth(1L, 2025, 3))
                    .thenReturn(List.of(attendance));

            List<Attendance> result = attendanceService.findByStudentAndMonth(1L, 2025, 3);

            assertAll(
                    () -> verify(attendanceRepository).findByStudentIdAndYearAndMonth(1L, 2025, 3),
                    () -> assertEquals(List.of(attendance), result)
            );
        }
    }

    @Nested
    @DisplayName("3. save()")
    class Save {

        @Test
        @DisplayName("TC-3-1. 성공 → Attendance.of() 로 생성 후 save 호출, 반환값")
        void success() {
            CreateAttendanceRequest request = CreateAttendanceRequest.of(
                    LocalDate.of(2025, 3, 10), AttendanceStatus.ABSENT, "감기");
            Attendance saved = mock(Attendance.class);
            when(attendanceRepository.save(any())).thenReturn(saved);

            Attendance result = attendanceService.save(student, request, teacher);

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals(student, captor.getValue().getStudent()),
                    () -> assertEquals(LocalDate.of(2025, 3, 10), captor.getValue().getDate()),
                    () -> assertEquals(AttendanceStatus.ABSENT, captor.getValue().getStatus()),
                    () -> assertEquals("감기", captor.getValue().getNote()),
                    () -> assertEquals(teacher, captor.getValue().getCreatedBy()),
                    () -> assertEquals(saved, result)
            );
        }
    }

    @Nested
    @DisplayName("4. update()")
    class Update {

        @Test
        @DisplayName("TC-4-1. 성공 → attendance.update() 호출, 반환값")
        void success() {
            Attendance attendance = mock(Attendance.class);
            UpdateAttendanceRequest request = UpdateAttendanceRequest.of(
                    LocalDate.of(2025, 3, 11), AttendanceStatus.LATE, "지각");

            Attendance result = attendanceService.update(attendance, request);

            assertAll(
                    () -> verify(attendance).update(LocalDate.of(2025, 3, 11), AttendanceStatus.LATE, "지각"),
                    () -> assertEquals(attendance, result)
            );
        }
    }

    @Nested
    @DisplayName("5. delete()")
    class Delete {

        @Test
        @DisplayName("TC-5-1. 성공 → repository.delete() 호출")
        void success() {
            Attendance attendance = mock(Attendance.class);

            attendanceService.delete(attendance);

            verify(attendanceRepository).delete(attendance);
        }
    }
}
