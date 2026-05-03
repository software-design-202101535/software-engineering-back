package com.example.EduManager.domain.counseling.service;

import com.example.EduManager.domain.counseling.dto.CreateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.EduManager.domain.counseling.dto.UpdateCounselingShareRequest;
import com.example.EduManager.domain.counseling.entity.Counseling;
import com.example.EduManager.domain.counseling.repository.CounselingRepository;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
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
@DisplayName("CounselingService 단위 테스트")
class CounselingServiceTest {

    @Mock CounselingRepository counselingRepository;

    @InjectMocks
    CounselingService counselingService;

    @Mock StudentProfile student;
    @Mock TeacherProfile teacher;

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

    @Nested
    @DisplayName("2. findForTeacherByYear()")
    class FindForTeacherByYear {

        @Test
        @DisplayName("TC-2-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Counseling counseling = mock(Counseling.class);
            when(counselingRepository.findByStudentForTeacherByYear(1L, 10L, 2025))
                    .thenReturn(List.of(counseling));

            List<Counseling> result = counselingService.findForTeacherByYear(1L, 10L, 2025);

            assertAll(
                    () -> verify(counselingRepository).findByStudentForTeacherByYear(1L, 10L, 2025),
                    () -> assertEquals(List.of(counseling), result)
            );
        }
    }

    @Nested
    @DisplayName("3. findForTeacherByYearAndMonth()")
    class FindForTeacherByYearAndMonth {

        @Test
        @DisplayName("TC-3-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Counseling counseling = mock(Counseling.class);
            when(counselingRepository.findByStudentForTeacherByYearAndMonth(1L, 10L, 2025, 3))
                    .thenReturn(List.of(counseling));

            List<Counseling> result = counselingService.findForTeacherByYearAndMonth(1L, 10L, 2025, 3);

            assertAll(
                    () -> verify(counselingRepository).findByStudentForTeacherByYearAndMonth(1L, 10L, 2025, 3),
                    () -> assertEquals(List.of(counseling), result)
            );
        }
    }

    @Nested
    @DisplayName("4. save()")
    class Save {

        @Test
        @DisplayName("TC-4-1. 성공 → Counseling.of()로 생성 후 save 호출, 반환값")
        void success() {
            CreateCounselingRequest request = CreateCounselingRequest.of(
                    LocalDate.of(2025, 3, 10), "상담 내용", "다음 계획",
                    LocalDate.of(2025, 4, 10), true);
            Counseling saved = mock(Counseling.class);
            when(counselingRepository.save(any())).thenReturn(saved);

            Counseling result = counselingService.save(student, teacher, request);

            ArgumentCaptor<Counseling> captor = ArgumentCaptor.forClass(Counseling.class);
            verify(counselingRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals(student, captor.getValue().getStudent()),
                    () -> assertEquals(teacher, captor.getValue().getTeacher()),
                    () -> assertEquals(LocalDate.of(2025, 3, 10), captor.getValue().getDate()),
                    () -> assertEquals("상담 내용", captor.getValue().getContent()),
                    () -> assertEquals("다음 계획", captor.getValue().getNextPlan()),
                    () -> assertEquals(LocalDate.of(2025, 4, 10), captor.getValue().getNextDate()),
                    () -> assertTrue(captor.getValue().isSharedWithTeachers()),
                    () -> assertEquals(saved, result)
            );
        }
    }

    @Nested
    @DisplayName("5. update()")
    class Update {

        @Test
        @DisplayName("TC-5-1. 성공 → counseling.update() 호출, 반환값")
        void success() {
            Counseling counseling = mock(Counseling.class);
            UpdateCounselingRequest request = UpdateCounselingRequest.of(
                    LocalDate.of(2025, 3, 10), "수정 내용", "수정 계획",
                    LocalDate.of(2025, 4, 10), false);

            Counseling result = counselingService.update(counseling, request);

            assertAll(
                    () -> verify(counseling).update("수정 내용", "수정 계획", LocalDate.of(2025, 4, 10)),
                    () -> assertEquals(counseling, result)
            );
        }
    }

    @Nested
    @DisplayName("6. updateSharedStatus()")
    class UpdateSharedStatus {

        @Test
        @DisplayName("TC-6-1. 성공 → counseling.updateSharedStatus() 호출, 반환값")
        void success() {
            Counseling counseling = mock(Counseling.class);
            UpdateCounselingShareRequest request = UpdateCounselingShareRequest.of(true);

            Counseling result = counselingService.updateSharedStatus(counseling, request);

            assertAll(
                    () -> verify(counseling).updateSharedStatus(true),
                    () -> assertEquals(counseling, result)
            );
        }
    }

    @Nested
    @DisplayName("7. delete()")
    class Delete {

        @Test
        @DisplayName("TC-7-1. 성공 → repository.delete() 호출")
        void success() {
            Counseling counseling = mock(Counseling.class);

            counselingService.delete(counseling);

            verify(counselingRepository).delete(counseling);
        }
    }
}
