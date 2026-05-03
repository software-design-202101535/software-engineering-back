package com.example.EduManager.domain.feedback.service;

import com.example.EduManager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.EduManager.domain.feedback.entity.Feedback;
import com.example.EduManager.domain.feedback.entity.FeedbackCategory;
import com.example.EduManager.domain.feedback.repository.FeedbackRepository;
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
@DisplayName("FeedbackService 단위 테스트")
class FeedbackServiceTest {

    @Mock FeedbackRepository feedbackRepository;

    @InjectMocks
    FeedbackService feedbackService;

    @Mock StudentProfile student;
    @Mock TeacherProfile teacher;

    @Nested
    @DisplayName("1. getByIdAndStudentId()")
    class GetByIdAndStudentId {

        @Test
        @DisplayName("TC-1-1. 존재하지 않음 → FEEDBACK_NOT_FOUND")
        void notFound() {
            when(feedbackRepository.findByIdAndStudentId(999L, 1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> feedbackService.getByIdAndStudentId(999L, 1L));

            assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("TC-1-2. 성공 → Feedback 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findByIdAndStudentId(1L, 1L)).thenReturn(Optional.of(feedback));

            assertEquals(feedback, feedbackService.getByIdAndStudentId(1L, 1L));
        }
    }

    @Nested
    @DisplayName("2. findAll()")
    class FindAll {

        @Test
        @DisplayName("TC-2-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentId(1L)).thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findAll(1L);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentId(1L),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("3. findAllByCategory()")
    class FindAllByCategory {

        @Test
        @DisplayName("TC-3-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentIdAndCategory(1L, FeedbackCategory.GRADE))
                    .thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findAllByCategory(1L, FeedbackCategory.GRADE);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentIdAndCategory(1L, FeedbackCategory.GRADE),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("4. findStudentVisible()")
    class FindStudentVisible {

        @Test
        @DisplayName("TC-4-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentIdAndStudentVisibleTrue(1L)).thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findStudentVisible(1L);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentIdAndStudentVisibleTrue(1L),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("5. findStudentVisibleByCategory()")
    class FindStudentVisibleByCategory {

        @Test
        @DisplayName("TC-5-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentIdAndStudentVisibleTrueAndCategory(1L, FeedbackCategory.ATTITUDE))
                    .thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findStudentVisibleByCategory(1L, FeedbackCategory.ATTITUDE);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentIdAndStudentVisibleTrueAndCategory(1L, FeedbackCategory.ATTITUDE),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("6. findParentVisible()")
    class FindParentVisible {

        @Test
        @DisplayName("TC-6-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentIdAndParentVisibleTrue(1L)).thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findParentVisible(1L);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentIdAndParentVisibleTrue(1L),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("7. findParentVisibleByCategory()")
    class FindParentVisibleByCategory {

        @Test
        @DisplayName("TC-7-1. 성공 → repository 위임, 결과 반환")
        void success() {
            Feedback feedback = mock(Feedback.class);
            when(feedbackRepository.findAllByStudentIdAndParentVisibleTrueAndCategory(1L, FeedbackCategory.ATTENDANCE))
                    .thenReturn(List.of(feedback));

            List<Feedback> result = feedbackService.findParentVisibleByCategory(1L, FeedbackCategory.ATTENDANCE);

            assertAll(
                    () -> verify(feedbackRepository).findAllByStudentIdAndParentVisibleTrueAndCategory(1L, FeedbackCategory.ATTENDANCE),
                    () -> assertEquals(List.of(feedback), result)
            );
        }
    }

    @Nested
    @DisplayName("8. save()")
    class Save {

        @Test
        @DisplayName("TC-8-1. 성공 → Feedback.of()로 생성 후 save 호출, 반환값")
        void success() {
            CreateFeedbackRequest request = CreateFeedbackRequest.of(
                    FeedbackCategory.GRADE, LocalDate.of(2025, 3, 10), "피드백 내용", true, false);
            Feedback saved = mock(Feedback.class);
            when(feedbackRepository.save(any())).thenReturn(saved);

            Feedback result = feedbackService.save(student, teacher, request);

            ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
            verify(feedbackRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals(student, captor.getValue().getStudent()),
                    () -> assertEquals(teacher, captor.getValue().getTeacher()),
                    () -> assertEquals(FeedbackCategory.GRADE, captor.getValue().getCategory()),
                    () -> assertEquals(LocalDate.of(2025, 3, 10), captor.getValue().getDate()),
                    () -> assertEquals("피드백 내용", captor.getValue().getContent()),
                    () -> assertTrue(captor.getValue().isStudentVisible()),
                    () -> assertFalse(captor.getValue().isParentVisible()),
                    () -> assertEquals(saved, result)
            );
        }
    }

    @Nested
    @DisplayName("9. update()")
    class Update {

        @Test
        @DisplayName("TC-9-1. 성공 → feedback.update() 호출, 반환값")
        void success() {
            Feedback feedback = mock(Feedback.class);
            UpdateFeedbackRequest request = UpdateFeedbackRequest.of(
                    FeedbackCategory.ATTITUDE, LocalDate.of(2025, 3, 11), "수정 내용", true, true);

            Feedback result = feedbackService.update(feedback, request);

            assertAll(
                    () -> verify(feedback).update(FeedbackCategory.ATTITUDE, LocalDate.of(2025, 3, 11), "수정 내용", true, true),
                    () -> assertEquals(feedback, result)
            );
        }
    }

    @Nested
    @DisplayName("10. updateVisibility()")
    class UpdateVisibility {

        @Test
        @DisplayName("TC-10-1. 성공 → feedback.updateVisibility() 호출, 반환값")
        void success() {
            Feedback feedback = mock(Feedback.class);
            UpdateFeedbackVisibilityRequest request = UpdateFeedbackVisibilityRequest.of(false, true);

            Feedback result = feedbackService.updateVisibility(feedback, request);

            assertAll(
                    () -> verify(feedback).updateVisibility(false, true),
                    () -> assertEquals(feedback, result)
            );
        }
    }

    @Nested
    @DisplayName("11. delete()")
    class Delete {

        @Test
        @DisplayName("TC-11-1. 성공 → repository.delete() 호출")
        void success() {
            Feedback feedback = mock(Feedback.class);

            feedbackService.delete(feedback);

            verify(feedbackRepository).delete(feedback);
        }
    }
}
